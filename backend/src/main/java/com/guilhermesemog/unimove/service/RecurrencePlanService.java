package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.recurrence.*;
import com.guilhermesemog.unimove.exception.type.IllegalUpdateException;
import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.RecurrencePlanMapper;
import com.guilhermesemog.unimove.model.InterestList;
import com.guilhermesemog.unimove.model.RecurrencePlan;
import com.guilhermesemog.unimove.model.University;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.enums.*;
import com.guilhermesemog.unimove.repository.InterestListRepository;
import com.guilhermesemog.unimove.repository.RecurrencePlanRepository;
import com.guilhermesemog.unimove.repository.UniversityRepository;
import com.guilhermesemog.unimove.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RecurrencePlanService {
    private final RecurrencePlanRepository planRepository;
    private final InterestListRepository interestListRepository;
    private final UniversityRepository universityRepository;
    private final UserRepository userRepository;
    private final RecurrencePlanningEngine planningEngine;
    private final RecurrencePlanMapper mapper;
    private final BusinessEventPublisher eventPublisher;
    private final AuditService auditService;
    private final Clock businessClock;
    private final int defaultHorizonWeeks;

    public RecurrencePlanService(
            RecurrencePlanRepository planRepository,
            InterestListRepository interestListRepository,
            UniversityRepository universityRepository,
            UserRepository userRepository,
            RecurrencePlanningEngine planningEngine,
            RecurrencePlanMapper mapper,
            BusinessEventPublisher eventPublisher,
            AuditService auditService,
            Clock businessClock,
            @Value("${unimove.phase-five.recurrence.default-horizon-weeks:8}") int defaultHorizonWeeks
    ) {
        this.planRepository = planRepository;
        this.interestListRepository = interestListRepository;
        this.universityRepository = universityRepository;
        this.userRepository = userRepository;
        this.planningEngine = planningEngine;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
        this.businessClock = businessClock;
        this.defaultHorizonWeeks = defaultHorizonWeeks;
    }

    @Transactional(readOnly = true)
    public RecurrencePreviewResponse preview(RecurrencePlanRequest request) {
        validate(request);
        requireDestination(request.destinationId());
        return planningEngine.preview(request, null);
    }

    @Transactional
    public RecurrencePlanResponse create(RecurrencePlanRequest request) {
        validate(request);
        RecurrencePlan plan = new RecurrencePlan();
        apply(plan, request);
        plan.setStatus(RecurrencePlanStatus.ACTIVE);
        plan.setCreatedBy(currentUser());
        plan.setNextEligibleDate(planningEngine.firstEligibleDate(
                max(request.startDate(), LocalDate.now(businessClock)), request.endDate(), request.daysOfWeek()));
        RecurrencePlan saved = planRepository.save(plan);
        publishPlanEvent(saved, BusinessEventType.RECURRENCE_PLAN_CREATED);
        auditService.record(AuditAction.RECURRENCE_PLAN_CREATED, "RecurrencePlan", saved.getId(),
                Map.of(), state(saved), Map.of());
        return response(saved);
    }

    @Transactional(readOnly = true)
    public RecurrencePlanResponse getById(UUID id) {
        return response(requirePlan(id));
    }

    @Transactional(readOnly = true)
    public Page<RecurrencePlanResponse> getAll(int page, int size, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by("createdAt").descending() : Sort.by("createdAt").ascending();
        return planRepository.findAll(PageRequest.of(page, size, sort)).map(this::response);
    }

    @Transactional
    public RecurrencePlanResponse update(UUID id, RecurrencePlanRequest request) {
        validate(request);
        RecurrencePlan plan = requirePlan(id);
        ensureNotArchived(plan);
        Map<String, Object> previous = state(plan);
        apply(plan, request);
        plan.setNextEligibleDate(planningEngine.firstEligibleDate(
                max(request.startDate(), LocalDate.now(businessClock)), request.endDate(), request.daysOfWeek()));
        RecurrencePlan saved = planRepository.save(plan);
        publishPlanEvent(saved, BusinessEventType.RECURRENCE_PLAN_UPDATED);
        auditService.record(AuditAction.RECURRENCE_PLAN_UPDATED, "RecurrencePlan", saved.getId(),
                previous, state(saved), Map.of("publishedOccurrencesPreserved", true));
        return response(saved);
    }

    @Transactional
    public RecurrencePlanResponse updateStatus(UUID id, RecurrenceStatusUpdate request) {
        if (request.status() == RecurrencePlanStatus.ARCHIVED) {
            throw new IllegalUpdateException("Use the archive operation to archive a recurrence plan");
        }
        RecurrencePlan plan = requirePlan(id);
        ensureNotArchived(plan);
        if (plan.getStatus() == request.status()) {
            return response(plan);
        }
        Map<String, Object> previous = state(plan);
        plan.setStatus(request.status());
        if (request.status() == RecurrencePlanStatus.ACTIVE) {
            plan.setNextEligibleDate(planningEngine.firstEligibleDate(
                    max(plan.getStartDate(), LocalDate.now(businessClock)), plan.getEndDate(), plan.getDaysOfWeek()));
        }
        RecurrencePlan saved = planRepository.save(plan);
        publishPlanEvent(saved, BusinessEventType.RECURRENCE_PLAN_STATUS_CHANGED);
        auditService.record(AuditAction.RECURRENCE_PLAN_STATUS_CHANGED, "RecurrencePlan", saved.getId(),
                previous, state(saved), Map.of());
        return response(saved);
    }

    @Transactional
    public void archive(UUID id) {
        RecurrencePlan plan = requirePlan(id);
        if (plan.getStatus() == RecurrencePlanStatus.ARCHIVED) return;
        Map<String, Object> previous = state(plan);
        plan.setStatus(RecurrencePlanStatus.ARCHIVED);
        plan.setNextEligibleDate(null);
        RecurrencePlan saved = planRepository.save(plan);
        publishPlanEvent(saved, BusinessEventType.RECURRENCE_PLAN_STATUS_CHANGED);
        auditService.record(AuditAction.RECURRENCE_PLAN_ARCHIVED, "RecurrencePlan", saved.getId(),
                previous, state(saved), Map.of("publishedOccurrencesPreserved", true));
    }

    @Transactional
    public RecurrenceGenerationResponse generate(UUID id) {
        RecurrencePlan plan = requirePlan(id);
        if (plan.getStatus() != RecurrencePlanStatus.ACTIVE) {
            throw new IllegalUpdateException("Only active recurrence plans can generate demands");
        }
        RecurrencePreviewResponse preview = planningEngine.preview(plan);
        List<RecurrenceOccurrenceResponse> results = new java.util.ArrayList<>();
        int created = 0;

        for (RecurrenceOccurrenceResponse occurrence : preview.occurrences()) {
            if (occurrence.action() != RecurrenceOccurrenceAction.CREATE) {
                results.add(occurrence);
                continue;
            }
            UUID demandId = UUID.randomUUID();
            int inserted = interestListRepository.insertGeneratedOccurrence(
                    demandId, occurrence.date(), plan.getClosingTime(), plan.getDepartureTime(), plan.getArrivalTime(),
                    plan.getReturnDepartureTime(), plan.getReturnArrivalTime(), plan.getDestination().getId(),
                    businessClock.instant(), plan.getId()
            );
            if (inserted == 0) {
                results.add(new RecurrenceOccurrenceResponse(occurrence.date(), RecurrenceOccurrenceAction.SKIP,
                        null, "Created by a concurrent generation"));
                continue;
            }
            created++;
            results.add(new RecurrenceOccurrenceResponse(occurrence.date(), RecurrenceOccurrenceAction.CREATE, demandId, null));
            Map<String, Object> demand = demandState(plan, demandId, occurrence.date());
            eventPublisher.publish(BusinessEventType.DEMAND_PUBLISHED, "InterestList", demandId, demand,
                    BusinessEventType.DEMAND_PUBLISHED + ":" + demandId);
            auditService.record(AuditAction.DEMAND_PUBLISHED, "InterestList", demandId,
                    Map.of(), demand, Map.of("recurrencePlanId", plan.getId()));
        }

        plan.setNextEligibleDate(planningEngine.firstEligibleDate(
                preview.rangeEnd().plusDays(1), plan.getEndDate(), plan.getDaysOfWeek()));
        planRepository.save(plan);
        int skipped = (int) results.stream().filter(item -> item.action() == RecurrenceOccurrenceAction.SKIP).count();
        int conflicts = (int) results.stream().filter(item -> item.action() == RecurrenceOccurrenceAction.CONFLICT).count();
        Map<String, Object> metadata = Map.of("created", created, "skipped", skipped, "conflicts", conflicts,
                "rangeStart", preview.rangeStart(), "rangeEnd", preview.rangeEnd());
        if (created > 0) {
            eventPublisher.publish(BusinessEventType.RECURRENCE_GENERATION_COMPLETED, "RecurrencePlan", plan.getId(), metadata,
                    BusinessEventType.RECURRENCE_GENERATION_COMPLETED + ":" + plan.getId() + ":" + preview.rangeEnd());
        }
        auditService.record(AuditAction.RECURRENCE_PLAN_GENERATED, "RecurrencePlan", plan.getId(),
                Map.of(), state(plan), metadata);
        return new RecurrenceGenerationResponse(plan.getId(), created, skipped, conflicts, List.copyOf(results));
    }

    public List<RecurrencePlan> activePlans() {
        return planRepository.findAllByStatusOrderByNextEligibleDateAsc(RecurrencePlanStatus.ACTIVE);
    }

    private void apply(RecurrencePlan plan, RecurrencePlanRequest request) {
        plan.setName(request.name().trim());
        plan.setDestination(requireDestination(request.destinationId()));
        plan.setDaysOfWeek(new java.util.LinkedHashSet<>(request.daysOfWeek()));
        plan.setStartDate(request.startDate());
        plan.setEndDate(request.endDate());
        plan.setClosingTime(request.closingTime());
        plan.setDepartureTime(request.departureTime());
        plan.setArrivalTime(request.arrivalTime());
        plan.setReturnDepartureTime(request.returnDepartureTime());
        plan.setReturnArrivalTime(request.returnArrivalTime());
        plan.setHorizonWeeks(request.horizonWeeks() == null ? defaultHorizonWeeks : request.horizonWeeks());
    }

    private void validate(RecurrencePlanRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalUpdateException("End date must be on or after start date");
        }
        if (request.endDate().isBefore(LocalDate.now(businessClock))) {
            throw new IllegalUpdateException("A recurrence plan must include a future date");
        }
    }

    private RecurrencePlanResponse response(RecurrencePlan plan) {
        long future = interestListRepository.countByRecurrencePlan_IdAndReferenceDateGreaterThanEqual(
                plan.getId(), LocalDate.now(businessClock));
        return mapper.toResponse(plan, future);
    }

    private RecurrencePlan requirePlan(UUID id) {
        return planRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Recurrence plan not found"));
    }

    private University requireDestination(UUID id) {
        return universityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("University not found"));
    }

    private User currentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;
        return userRepository.findByCpf(authentication.getName()).orElse(null);
    }

    private void ensureNotArchived(RecurrencePlan plan) {
        if (plan.getStatus() == RecurrencePlanStatus.ARCHIVED) {
            throw new IllegalUpdateException("Archived recurrence plans cannot be changed");
        }
    }

    private void publishPlanEvent(RecurrencePlan plan, BusinessEventType type) {
        eventPublisher.publish(type, "RecurrencePlan", plan.getId(), state(plan),
                type + ":" + plan.getId() + ":" + plan.getVersion());
    }

    private Map<String, Object> state(RecurrencePlan plan) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("name", plan.getName());
        state.put("destinationId", plan.getDestination().getId());
        state.put("daysOfWeek", plan.getDaysOfWeek());
        state.put("startDate", plan.getStartDate());
        state.put("endDate", plan.getEndDate());
        state.put("closingTime", plan.getClosingTime());
        state.put("departureTime", plan.getDepartureTime());
        state.put("arrivalTime", plan.getArrivalTime());
        state.put("returnDepartureTime", plan.getReturnDepartureTime());
        state.put("returnArrivalTime", plan.getReturnArrivalTime());
        state.put("horizonWeeks", plan.getHorizonWeeks());
        state.put("status", plan.getStatus());
        state.put("nextEligibleDate", plan.getNextEligibleDate());
        return state;
    }

    private Map<String, Object> demandState(RecurrencePlan plan, UUID id, LocalDate date) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("interestListId", id);
        state.put("referenceDate", date);
        state.put("closingTime", plan.getClosingTime());
        state.put("departureTime", plan.getDepartureTime());
        state.put("arrivalTime", plan.getArrivalTime());
        state.put("returnDepartureTime", plan.getReturnDepartureTime());
        state.put("returnArrivalTime", plan.getReturnArrivalTime());
        state.put("destinationId", plan.getDestination().getId());
        state.put("status", ListStatus.OPEN);
        state.put("recurrencePlanId", plan.getId());
        return state;
    }

    private LocalDate max(LocalDate first, LocalDate second) {
        return first.isAfter(second) ? first : second;
    }
}
