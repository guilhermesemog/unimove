package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.audit.*;
import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import com.guilhermesemog.unimove.model.*;
import com.guilhermesemog.unimove.model.enums.AuditAction;
import com.guilhermesemog.unimove.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AuditQueryService {
    private static final Map<AuditAction, String> ACTION_LABELS = Map.ofEntries(
            Map.entry(AuditAction.DEMAND_PUBLISHED, "Demand published"),
            Map.entry(AuditAction.DEMAND_UPDATED, "Demand updated"),
            Map.entry(AuditAction.DEMAND_STATUS_CHANGED, "Demand status changed"),
            Map.entry(AuditAction.TRIP_CREATED, "Trip created"),
            Map.entry(AuditAction.TRIP_ASSIGNMENT_CHANGED, "Trip assignment changed"),
            Map.entry(AuditAction.BOOKING_CREATED, "Booking created"),
            Map.entry(AuditAction.BOOKING_REACTIVATED, "Booking reactivated"),
            Map.entry(AuditAction.BOOKING_CANCELLED, "Booking cancelled"),
            Map.entry(AuditAction.USER_ACTIVATED, "User activated"),
            Map.entry(AuditAction.USER_DEACTIVATED, "User deactivated"),
            Map.entry(AuditAction.RECURRENCE_PLAN_CREATED, "Recurring plan created"),
            Map.entry(AuditAction.RECURRENCE_PLAN_UPDATED, "Recurring plan updated"),
            Map.entry(AuditAction.RECURRENCE_PLAN_STATUS_CHANGED, "Recurring plan status changed"),
            Map.entry(AuditAction.RECURRENCE_PLAN_ARCHIVED, "Recurring plan archived"),
            Map.entry(AuditAction.RECURRENCE_PLAN_GENERATED, "Recurring demands generated")
    );

    private final AuditEventRepository auditRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final UniversityRepository universityRepository;
    private final ObjectMapper objectMapper;

    public AuditQueryService(AuditEventRepository auditRepository, UserRepository userRepository,
                             VehicleRepository vehicleRepository, UniversityRepository universityRepository,
                             ObjectMapper objectMapper) {
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.universityRepository = universityRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Page<AuditEventResponse> search(AuditQuery query, int page, int size, String direction) {
        validateRange(query.from(), query.to());
        Sort sort = "asc".equalsIgnoreCase(direction)
                ? Sort.by("occurredAt").ascending().and(Sort.by("id").ascending())
                : Sort.by("occurredAt").descending().and(Sort.by("id").descending());
        Page<AuditEvent> events = auditRepository.findAll(specification(query),
                PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), sort));
        ResolutionContext context = resolutionContext(events.getContent());
        return events.map(event -> response(event, context));
    }

    @Transactional(readOnly = true)
    public AuditEventDetailResponse getById(UUID id) {
        AuditEvent event = auditRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit event not found"));
        AuditEventResponse response = response(event, resolutionContext(List.of(event)));
        return new AuditEventDetailResponse(response, parse(event.getPreviousState()),
                parse(event.getResultingState()), parse(event.getMetadata()));
    }

    @Transactional(readOnly = true)
    public Set<UUID> findActorIds(String actor) {
        if (actor == null || actor.isBlank()) return Set.of();
        try {
            return Set.of(UUID.fromString(actor.trim()));
        } catch (IllegalArgumentException ignored) {
            return userRepository.findAllByFullName(actor.trim(), PageRequest.of(0, 100, Sort.by("firstName")))
                    .stream().map(User::getId).collect(Collectors.toSet());
        }
    }

    private Specification<AuditEvent> specification(AuditQuery query) {
        return (root, criteriaQuery, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.from() != null) predicates.add(builder.greaterThanOrEqualTo(root.get("occurredAt"), query.from()));
            if (query.to() != null) predicates.add(builder.lessThan(root.get("occurredAt"), query.to()));
            if (query.actorIds() != null && !query.actorIds().isEmpty()) predicates.add(root.get("actorId").in(query.actorIds()));
            if (query.action() != null) predicates.add(builder.equal(root.get("action"), query.action()));
            if (query.entityType() != null && !query.entityType().isBlank())
                predicates.add(builder.equal(builder.lower(root.get("entityType")), query.entityType().trim().toLowerCase(Locale.ROOT)));
            if (query.entityId() != null) predicates.add(builder.equal(root.get("entityId"), query.entityId()));
            else if (query.entityIds() != null && !query.entityIds().isEmpty()) predicates.add(root.get("entityId").in(query.entityIds()));
            if (query.correlationId() != null) predicates.add(builder.equal(root.get("correlationId"), query.correlationId()));
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void validateRange(Instant from, Instant to) {
        if (from != null && to != null && !from.isBefore(to)) {
            throw new IllegalArgumentException("Audit start must be before end");
        }
    }

    private AuditEventResponse response(AuditEvent event, ResolutionContext context) {
        Map<String, Object> before = parse(event.getPreviousState());
        Map<String, Object> after = parse(event.getResultingState());
        List<AuditChangeResponse> changes = changes(before, after, context);
        String actionLabel = ACTION_LABELS.getOrDefault(event.getAction(), titleCase(event.getAction().name()));
        String actorName = event.getActorId() == null ? "System" : context.userNames().getOrDefault(event.getActorId(), "Former user");
        boolean creation = event.getAction().name().endsWith("CREATED") || event.getAction() == AuditAction.DEMAND_PUBLISHED;
        String description = changes.isEmpty() || creation ? actionLabel : changes.getFirst().label() + " changed from "
                + changes.getFirst().previousValue() + " to " + changes.getFirst().resultingValue();
        return new AuditEventResponse(event.getId(), event.getActorId(), actorName, event.getActorRole(),
                event.getAction(), actionLabel, event.getEntityType(), event.getEntityId(), event.getOccurredAt(),
                event.getCorrelationId(), description, changes);
    }

    private List<AuditChangeResponse> changes(Map<String, Object> before, Map<String, Object> after,
                                              ResolutionContext context) {
        LinkedHashSet<String> fields = new LinkedHashSet<>(before.keySet());
        fields.addAll(after.keySet());
        return fields.stream().filter(field -> !Objects.equals(before.get(field), after.get(field)))
                .map(field -> new AuditChangeResponse(field, fieldLabel(field),
                        displayValue(field, before.get(field), context), displayValue(field, after.get(field), context)))
                .toList();
    }

    private String displayValue(String field, Object value, ResolutionContext context) {
        if (value == null) return "None";
        UUID id = asUuid(value);
        if (id != null && field.equals("conductorId")) return context.userNames().getOrDefault(id, shortId(id));
        if (id != null && field.equals("vehicleId")) return context.vehicleNames().getOrDefault(id, shortId(id));
        if (id != null && field.equals("destinationId")) return context.universityNames().getOrDefault(id, shortId(id));
        if (value instanceof Collection<?> values) return values.stream().map(String::valueOf).collect(Collectors.joining(", "));
        return String.valueOf(value).replace('_', ' ');
    }

    private ResolutionContext resolutionContext(List<AuditEvent> events) {
        Set<UUID> userIds = new HashSet<>();
        Set<UUID> vehicleIds = new HashSet<>();
        Set<UUID> universityIds = new HashSet<>();
        events.forEach(event -> {
            if (event.getActorId() != null) userIds.add(event.getActorId());
            collectReferences(parse(event.getPreviousState()), userIds, vehicleIds, universityIds);
            collectReferences(parse(event.getResultingState()), userIds, vehicleIds, universityIds);
        });
        return new ResolutionContext(
                userRepository.findAllById(userIds).stream().collect(Collectors.toMap(User::getId,
                        user -> user.getFirstName() + " " + user.getLastName())),
                vehicleRepository.findAllById(vehicleIds).stream().collect(Collectors.toMap(Vehicle::getId, Vehicle::getPlate)),
                universityRepository.findAllById(universityIds).stream().collect(Collectors.toMap(University::getId, University::getName))
        );
    }

    private void collectReferences(Map<String, Object> state, Set<UUID> users, Set<UUID> vehicles,
                                   Set<UUID> universities) {
        addUuid(state.get("conductorId"), users);
        addUuid(state.get("vehicleId"), vehicles);
        addUuid(state.get("destinationId"), universities);
    }

    private void addUuid(Object value, Set<UUID> target) {
        UUID id = asUuid(value);
        if (id != null) target.add(id);
    }

    private UUID asUuid(Object value) {
        if (value == null) return null;
        try { return UUID.fromString(String.valueOf(value)); }
        catch (IllegalArgumentException ignored) { return null; }
    }

    private Map<String, Object> parse(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try { return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {}); }
        catch (Exception exception) { throw new IllegalStateException("Could not read persisted audit data", exception); }
    }

    private String fieldLabel(String field) {
        return switch (field) {
            case "conductorId" -> "Driver";
            case "vehicleId" -> "Vehicle";
            case "destinationId" -> "Destination";
            case "referenceDate" -> "Travel date";
            case "closingTime" -> "Booking deadline";
            case "departureTime" -> "Departure";
            case "arrivalTime" -> "Arrival";
            case "returnDepartureTime" -> "Return departure";
            case "returnArrivalTime" -> "Return arrival";
            case "bookingStatus" -> "Booking status";
            case "listStatus", "status" -> "Status";
            case "horizonWeeks" -> "Planning horizon";
            case "nextEligibleDate" -> "Next eligible date";
            default -> titleCase(field.replaceAll("([a-z])([A-Z])", "$1_$2"));
        };
    }

    private String titleCase(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replace('_', ' ');
        return normalized.isEmpty() ? normalized : Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private String shortId(UUID id) { return id.toString().substring(0, 8); }

    private record ResolutionContext(Map<UUID, String> userNames, Map<UUID, String> vehicleNames,
                                     Map<UUID, String> universityNames) {
    }
}
