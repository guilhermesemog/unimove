package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.recurrence.RecurrencePlanResponse;
import com.guilhermesemog.unimove.model.RecurrencePlan;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;

@Component
public class RecurrencePlanMapper {
    private final UniversityMapper universityMapper;

    public RecurrencePlanMapper(UniversityMapper universityMapper) {
        this.universityMapper = universityMapper;
    }

    public RecurrencePlanResponse toResponse(RecurrencePlan plan, long futureOccurrences) {
        return new RecurrencePlanResponse(
                plan.getId(), plan.getName(), universityMapper.toResponse(plan.getDestination()),
                new LinkedHashSet<>(plan.getDaysOfWeek()), plan.getStartDate(), plan.getEndDate(),
                plan.getClosingTime(), plan.getDepartureTime(), plan.getArrivalTime(),
                plan.getReturnDepartureTime(), plan.getReturnArrivalTime(), plan.getHorizonWeeks(),
                plan.getStatus(), plan.getNextEligibleDate(),
                plan.getCreatedBy() == null ? null : plan.getCreatedBy().getId(), futureOccurrences,
                plan.getCreatedAt(), plan.getUpdatedAt(), plan.getVersion()
        );
    }
}
