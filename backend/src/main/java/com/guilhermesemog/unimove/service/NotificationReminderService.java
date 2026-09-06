package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.model.Booking;
import com.guilhermesemog.unimove.model.InterestList;
import com.guilhermesemog.unimove.model.Trip;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.enums.BookingStatus;
import com.guilhermesemog.unimove.model.enums.ListStatus;
import com.guilhermesemog.unimove.model.enums.NotificationCategory;
import com.guilhermesemog.unimove.model.enums.NotificationType;
import com.guilhermesemog.unimove.model.enums.Role;
import com.guilhermesemog.unimove.repository.BookingRepository;
import com.guilhermesemog.unimove.repository.InterestListRepository;
import com.guilhermesemog.unimove.repository.TripRepository;
import com.guilhermesemog.unimove.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Service
public class NotificationReminderService {
    private final NotificationWriter writer;
    private final InterestListRepository interestListRepository;
    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final Clock businessClock;

    public NotificationReminderService(NotificationWriter writer,
                                       InterestListRepository interestListRepository,
                                       TripRepository tripRepository,
                                       BookingRepository bookingRepository,
                                       UserRepository userRepository,
                                       ObjectMapper objectMapper,
                                       Clock businessClock) {
        this.writer = writer;
        this.interestListRepository = interestListRepository;
        this.tripRepository = tripRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.businessClock = businessClock;
    }

    @Transactional
    public int createDueNotifications() {
        LocalDate today = LocalDate.now(businessClock);
        LocalDate tomorrow = today.plusDays(1);
        int created = 0;

        for (Trip trip : tripRepository.findAllByInterestList_ReferenceDateBetween(tomorrow, tomorrow)) {
            created += remindStudents(trip);
            created += remindConductor(trip);
            created += reportTripIssues(trip);
        }

        Instant now = businessClock.instant();
        Instant warningLimit = now.plusSeconds(24 * 60 * 60);
        for (InterestList demand : interestListRepository.findAllByListStatusAndReferenceDateBetween(
                ListStatus.OPEN, today, tomorrow)) {
            Instant closesAt = ZonedDateTime.of(demand.getReferenceDate(), demand.getClosingTime(),
                    businessClock.getZone()).toInstant();
            if (!closesAt.isBefore(now) && !closesAt.isAfter(warningLimit)
                    && tripRepository.findByInterestList_Id(demand.getId()).isEmpty()) {
                created += notifyAdmins("issue:demand-without-trip:" + demand.getId(),
                        "demand.without-trip", "Demand closing without a trip",
                        "An open demand is close to its booking deadline and has no trip yet.",
                        "/admin/interest-lists/" + demand.getId() + "/view",
                        Map.of("interestListId", demand.getId()));
            }
        }
        return created;
    }

    private int remindStudents(Trip trip) {
        int created = 0;
        List<Booking> bookings = bookingRepository.findAllByInterestList_IdAndBookingStatusNot(
                trip.getInterestList().getId(), BookingStatus.CANCELLED);
        for (Booking booking : bookings) {
            if (write(booking.getStudent().getUser(), NotificationType.REMINDER, NotificationCategory.REMINDER,
                    "student.trip.tomorrow", "Trip tomorrow",
                    "Your university trip is scheduled for tomorrow. Review the boarding details.",
                    "/bookings", "reminder:student:" + booking.getStudent().getId() + ":trip:" + trip.getId(),
                    Map.of("tripId", trip.getId(), "bookingId", booking.getId()))) created++;
        }
        return created;
    }

    private int remindConductor(Trip trip) {
        if (trip.getConductor() == null) return 0;
        return write(trip.getConductor().getUser(), NotificationType.REMINDER, NotificationCategory.REMINDER,
                "driver.operation.tomorrow", "Operation tomorrow",
                "You have an assigned operation tomorrow. Review its schedule and passenger manifest.",
                "/trips/" + trip.getId(), "reminder:driver:" + trip.getConductor().getId() + ":trip:" + trip.getId(),
                Map.of("tripId", trip.getId())) ? 1 : 0;
    }

    private int reportTripIssues(Trip trip) {
        int created = 0;
        String route = "/admin/trips/" + trip.getId() + "/view";
        if (trip.getConductor() == null || trip.getVehicle() == null) {
            created += notifyAdmins("issue:missing-assignment:" + trip.getId(), "trip.missing-assignment",
                    "Trip missing resources", "A trip scheduled for tomorrow needs a driver or vehicle assignment.",
                    route, Map.of("tripId", trip.getId()));
        }
        if (trip.getVehicle() != null) {
            int passengers = bookingRepository.findAllByInterestList_IdAndBookingStatusNot(
                    trip.getInterestList().getId(), BookingStatus.CANCELLED).size();
            if (passengers > trip.getVehicle().getCapacity()) {
                created += notifyAdmins("issue:capacity:" + trip.getId(), "trip.insufficient-capacity",
                        "Trip capacity is insufficient", "Active bookings exceed the assigned vehicle capacity.",
                        route, Map.of("tripId", trip.getId(), "passengers", passengers,
                                "capacity", trip.getVehicle().getCapacity()));
            }
        }
        return created;
    }

    private int notifyAdmins(String deduplicationPrefix, String contentKey, String title,
                             String description, String route, Map<String, ?> payload) {
        int created = 0;
        for (User admin : userRepository.findAllByRoleAndActiveTrue(Role.ADMIN)) {
            if (write(admin, NotificationType.ISSUE, NotificationCategory.ISSUE, contentKey, title,
                    description, route, deduplicationPrefix + ":admin:" + admin.getId(), payload)) created++;
        }
        return created;
    }

    private boolean write(User recipient, NotificationType type, NotificationCategory category,
                          String contentKey, String title, String description, String route,
                          String deduplicationKey, Map<String, ?> payload) {
        try {
            return writer.create(recipient, type, category, contentKey,
                    objectMapper.writeValueAsString(payload), title, description, route, deduplicationKey);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create scheduled notification", exception);
        }
    }
}
