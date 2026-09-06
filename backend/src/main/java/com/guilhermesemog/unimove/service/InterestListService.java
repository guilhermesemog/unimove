package com.guilhermesemog.unimove.service;

import java.util.UUID;
import com.guilhermesemog.unimove.dto.interestlist.*;
import com.guilhermesemog.unimove.exception.type.IllegalUpdateException;
import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.InterestListMapper;
import com.guilhermesemog.unimove.model.InterestList;
import com.guilhermesemog.unimove.model.University;
import com.guilhermesemog.unimove.model.enums.AuditAction;
import com.guilhermesemog.unimove.model.enums.ListStatus;
import com.guilhermesemog.unimove.model.enums.BusinessEventType;
import com.guilhermesemog.unimove.repository.BookingRepository;
import com.guilhermesemog.unimove.repository.InterestListRepository;
import com.guilhermesemog.unimove.repository.UniversityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class InterestListService {

    private final InterestListRepository interestListRepository;
    private final InterestListMapper interestListMapper;
    private final BookingRepository bookingRepository;
    private final UniversityRepository universityRepository;
    private final BusinessEventPublisher eventPublisher;
    private final AuditService auditService;
    private final Clock businessClock;

    public InterestListService(
            InterestListMapper interestListMapper,
            InterestListRepository interestListRepository,
            BookingRepository bookingRepository,
            UniversityRepository universityRepository,
            BusinessEventPublisher eventPublisher,
            AuditService auditService,
            Clock businessClock
    ) {
        this.interestListMapper = interestListMapper;
        this.interestListRepository = interestListRepository;
        this.bookingRepository = bookingRepository;
        this.universityRepository = universityRepository;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
        this.businessClock = businessClock;
    }

    @Transactional
    public InterestListResponse create(InterestListCreate requestBody) {
        University destination = getDestination(requestBody.destinationId());
        InterestList interestList = interestListMapper.toEntity(requestBody, destination);

        InterestList savedInterestList = interestListRepository.save(interestList);
        eventPublisher.publish(
                BusinessEventType.DEMAND_PUBLISHED,
                "InterestList",
                savedInterestList.getId(),
                demandPayload(savedInterestList),
                BusinessEventType.DEMAND_PUBLISHED.name() + ":" + savedInterestList.getId()
        );
        auditService.record(
                AuditAction.DEMAND_PUBLISHED,
                "InterestList",
                savedInterestList.getId(),
                Map.of(),
                demandAuditState(savedInterestList),
                Map.of()
        );
        return interestListMapper.toResponse(savedInterestList);
    }

    public InterestListResponse getById(UUID id) {
        InterestList interestList = getInterestList(id);

        return interestListMapper.toResponse(interestList);
    }

    public Page<InterestListResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return interestListRepository.findAll(pageable).map(interestListMapper::toResponse);
    }

    public void delete(UUID id) {
        InterestList interestList = getInterestList(id);

        if (bookingRepository.existsByInterestList_Id(id)) {
            throw new IllegalUpdateException("Cannot delete interest list with existing bookings");
        }

        interestListRepository.delete(interestList);
    }

    @Transactional
    public void update(UUID id, InterestListUpdate requestBody) {
        University destination = getDestination(requestBody.destinationId());
        InterestList interestList = getInterestList(id);

        Map<String, Object> previousState = demandAuditState(interestList);
        ListStatus previousStatus = interestList.getListStatus();
        interestListMapper.update(requestBody, destination, interestList);
        markStatusChange(previousStatus, interestList);
        InterestList savedInterestList = interestListRepository.save(interestList);
        publishDemandUpdate(savedInterestList);
        auditDemandUpdate(savedInterestList, previousState);
    }

    @Transactional
    public void update(UUID id, InterestListPatch requestBody) {
        University destination = getDestination(requestBody.destinationId());
        InterestList interestList = getInterestList(id);

        Map<String, Object> previousState = demandAuditState(interestList);
        ListStatus previousStatus = interestList.getListStatus();
        interestListMapper.update(requestBody, destination, interestList);
        markStatusChange(previousStatus, interestList);
        InterestList savedInterestList = interestListRepository.save(interestList);
        publishDemandUpdate(savedInterestList);
        auditDemandUpdate(savedInterestList, previousState);
    }

    @Transactional
    public void toggleStatus(UUID id, InterestListToggleStatus requestBody) {
        InterestList interestList = getInterestList(id);
        Map<String, Object> previousState = demandAuditState(interestList);

        if (requestBody.listStatus() != null) {
            interestList.setListStatus(requestBody.listStatus());
        } else {
            if (interestList.getListStatus() == ListStatus.OPEN) {
                interestList.setListStatus(ListStatus.PROCESSING);
            } else if (interestList.getListStatus() == ListStatus.PROCESSING) {
                interestList.setListStatus(ListStatus.CLOSED);
            }
        }

        interestList.setStatusChangedAt(businessClock.instant());
        InterestList savedInterestList = interestListRepository.save(interestList);
        eventPublisher.publish(
                BusinessEventType.DEMAND_STATUS_CHANGED,
                "InterestList",
                savedInterestList.getId(),
                Map.of(
                        "interestListId", savedInterestList.getId(),
                        "status", savedInterestList.getListStatus().name()
                ),
                BusinessEventType.DEMAND_STATUS_CHANGED.name() + ":" + savedInterestList.getId() + ":" + savedInterestList.getListStatus() + ":" + savedInterestList.getVersion()
        );
        auditService.record(
                AuditAction.DEMAND_STATUS_CHANGED,
                "InterestList",
                savedInterestList.getId(),
                previousState,
                demandAuditState(savedInterestList),
                Map.of()
        );
    }

    private void markStatusChange(ListStatus previousStatus, InterestList interestList) {
        if (previousStatus != interestList.getListStatus()) {
            interestList.setStatusChangedAt(businessClock.instant());
        }
    }

    private void publishDemandUpdate(InterestList interestList) {
        eventPublisher.publish(
                BusinessEventType.DEMAND_UPDATED,
                "InterestList",
                interestList.getId(),
                demandPayload(interestList),
                BusinessEventType.DEMAND_UPDATED.name() + ":" + interestList.getId() + ":" + interestList.getVersion()
        );
    }

    private Map<String, Object> demandPayload(InterestList interestList) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("interestListId", interestList.getId());
        payload.put("referenceDate", interestList.getReferenceDate().toString());
        payload.put("closingTime", interestList.getClosingTime() == null ? null : interestList.getClosingTime().toString());
        payload.put("departureTime", interestList.getDepartureTime().toString());
        payload.put("arrivalTime", interestList.getArrivalTime().toString());
        payload.put("returnDepartureTime", interestList.getReturnDepartureTime().toString());
        payload.put("returnArrivalTime", interestList.getReturnArrivalTime().toString());
        payload.put("destinationId", interestList.getDestination().getId());
        payload.put("status", interestList.getListStatus().name());
        return payload;
    }

    private void auditDemandUpdate(InterestList interestList, Map<String, Object> previousState) {
        auditService.record(
                AuditAction.DEMAND_UPDATED,
                "InterestList",
                interestList.getId(),
                previousState,
                demandAuditState(interestList),
                Map.of()
        );
    }

    private Map<String, Object> demandAuditState(InterestList interestList) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("referenceDate", interestList.getReferenceDate().toString());
        state.put("closingTime", interestList.getClosingTime() == null ? null : interestList.getClosingTime().toString());
        state.put("departureTime", interestList.getDepartureTime().toString());
        state.put("arrivalTime", interestList.getArrivalTime().toString());
        state.put("returnDepartureTime", interestList.getReturnDepartureTime().toString());
        state.put("returnArrivalTime", interestList.getReturnArrivalTime().toString());
        state.put("destinationId", interestList.getDestination().getId());
        state.put("status", interestList.getListStatus().name());
        return state;
    }

    private InterestList getInterestList(UUID id) {
        return interestListRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("InterestList not found"));
    }

    private University getDestination(UUID id) {
        return universityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("University not found"));
    }
}
