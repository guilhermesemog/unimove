package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.model.*;
import com.guilhermesemog.unimove.model.enums.*;
import com.guilhermesemog.unimove.repository.*;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class NotificationDispatchService {
    private final NotificationWriter writer;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final ObjectMapper objectMapper;

    public NotificationDispatchService(NotificationWriter writer, UserRepository userRepository,
                                       BookingRepository bookingRepository, TripRepository tripRepository,
                                       ObjectMapper objectMapper) {
        this.writer = writer;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
        this.objectMapper = objectMapper;
    }

    public int dispatch(OutboxEvent event) {
        JsonNode payload = read(event.getPayload());
        return switch (event.getEventType()) {
            case BOOKING_CREATED, BOOKING_REACTIVATED -> bookingNotification(event, payload, false);
            case BOOKING_CANCELLED -> bookingNotification(event, payload, true);
            case DEMAND_UPDATED -> demandNotification(event, payload, "demand.schedule.updated",
                    "Schedule updated", "The schedule for one of your booked trips has changed.",
                    NotificationType.DEMAND, true);
            case DEMAND_STATUS_CHANGED -> demandStatusNotification(event, payload);
            case TRIP_CREATED -> demandNotification(event, payload, "trip.confirmed",
                    "Trip confirmed", "Your booked trip has been confirmed by the operations team.",
                    NotificationType.TRIP, false);
            case TRIP_ASSIGNMENT_CHANGED -> assignmentNotifications(event, payload);
            case RECURRENCE_GENERATION_FAILED -> adminNotifications(event, "recurrence.generation.failed",
                    "Recurring generation needs attention", "A recurring plan could not generate its demand batch.",
                    "/admin/recurrence-plans");
            default -> 0;
        };
    }

    private int bookingNotification(OutboxEvent event, JsonNode payload, boolean cancelled) {
        UUID studentId = uuid(payload, "studentId");
        User user = userRepository.findById(studentId).orElse(null);
        String bookingId = text(payload, "bookingId");
        return create(event, user, NotificationType.BOOKING,
                cancelled ? NotificationCategory.INFO : NotificationCategory.ACTION,
                cancelled ? "booking.cancelled" : "booking.confirmed",
                cancelled ? "Booking cancelled" : "Booking confirmed",
                cancelled ? "Your booking was cancelled and remains available in your history."
                        : "Your seat is confirmed for the selected university trip.",
                "/bookings", Map.of("bookingId", bookingId)) ? 1 : 0;
    }

    private int demandStatusNotification(OutboxEvent event, JsonNode payload) {
        if (!"CLOSED".equals(text(payload, "status"))) return 0;
        return demandNotification(event, payload, "demand.closed", "Bookings closed",
                "Bookings have closed for one of your selected travel dates.", NotificationType.DEMAND, false);
    }

    private int demandNotification(OutboxEvent event, JsonNode payload, String key, String title,
                                   String description, NotificationType type, boolean notifyConductor) {
        UUID interestListId = uuid(payload, "interestListId");
        int created = 0;
        for (Booking booking : bookingRepository.findAllByInterestList_IdAndBookingStatusNot(
                interestListId, BookingStatus.CANCELLED)) {
            if (create(event, booking.getStudent().getUser(), type,
                    NotificationCategory.INFO, key, title, description, "/bookings",
                    Map.of("interestListId", interestListId))) created++;
        }
        if (notifyConductor) {
            Trip trip = tripRepository.findByInterestList_Id(interestListId).orElse(null);
            if (trip != null && trip.getConductor() != null
                    && create(event, trip.getConductor().getUser(), NotificationType.TRIP,
                    NotificationCategory.ACTION, "driver.schedule.updated", "Operation schedule updated",
                    "The schedule for an operation assigned to you has changed.", "/trips/" + trip.getId(),
                    Map.of("tripId", trip.getId(), "interestListId", interestListId))) created++;
        }
        return created;
    }

    private int assignmentNotifications(OutboxEvent event, JsonNode payload) {
        UUID tripId = uuid(payload, "tripId");
        UUID previousConductorId = nullableUuid(payload, "previousConductorId");
        UUID conductorId = nullableUuid(payload, "conductorId");
        int created = 0;

        if (previousConductorId != null && !previousConductorId.equals(conductorId)) {
            User previous = userRepository.findById(previousConductorId).orElse(null);
            if (create(event, previous, NotificationType.ASSIGNMENT, NotificationCategory.INFO,
                    "driver.assignment.removed", "Assignment removed",
                    "You are no longer assigned to this operation.", "/trips",
                    Map.of("tripId", tripId))) created++;
        }
        if (conductorId != null) {
            User current = userRepository.findById(conductorId).orElse(null);
            if (create(event, current, NotificationType.ASSIGNMENT, NotificationCategory.ACTION,
                    "driver.assignment.updated", "Operation assigned",
                    "A trip has been assigned to you. Review its schedule and manifest.", "/trips/" + tripId,
                    Map.of("tripId", tripId))) created++;
        }

        Trip trip = tripRepository.findById(tripId).orElse(null);
        if (trip != null) {
            for (Booking booking : bookingRepository.findAllByInterestList_IdAndBookingStatusNot(
                    trip.getInterestList().getId(), BookingStatus.CANCELLED)) {
                if (create(event, booking.getStudent().getUser(), NotificationType.ASSIGNMENT,
                        NotificationCategory.INFO, "trip.assignment.updated", "Trip details updated",
                        "Driver or vehicle information changed for your booked trip.", "/bookings",
                        Map.of("tripId", tripId))) created++;
            }
        }
        return created;
    }

    private int adminNotifications(OutboxEvent event, String key, String title, String description, String route) {
        int created = 0;
        for (User admin : userRepository.findAllByRoleAndActiveTrue(Role.ADMIN)) {
            if (create(event, admin, NotificationType.ISSUE, NotificationCategory.ISSUE,
                    key, title, description, route, Map.of("aggregateId", event.getAggregateId()))) created++;
        }
        return created;
    }

    private boolean create(OutboxEvent event, User recipient, NotificationType type,
                           NotificationCategory category, String contentKey, String title,
                           String description, String route, Map<String, ?> minimalPayload) {
        if (recipient == null) return false;
        try {
            return writer.create(recipient, type, category, contentKey,
                    objectMapper.writeValueAsString(minimalPayload), title, description, route,
                    event.getId() + ":" + recipient.getId() + ":" + contentKey);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create notification", exception);
        }
    }

    private JsonNode read(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid outbox payload", exception);
        }
    }

    private UUID uuid(JsonNode payload, String field) {
        UUID value = nullableUuid(payload, field);
        if (value == null) throw new IllegalArgumentException("Missing outbox field: " + field);
        return value;
    }

    private UUID nullableUuid(JsonNode payload, String field) {
        JsonNode value = payload.get(field);
        return value == null || value.isNull() || value.asText().isBlank() ? null : UUID.fromString(value.asText());
    }

    private String text(JsonNode payload, String field) {
        JsonNode value = payload.get(field);
        return value == null || value.isNull() ? "" : value.asText();
    }
}
