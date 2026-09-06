package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.RecurrencePlan;
import com.guilhermesemog.unimove.model.enums.RecurrencePlanStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecurrencePlanRepository extends JpaRepository<RecurrencePlan, UUID> {
    @EntityGraph(attributePaths = {"destination", "daysOfWeek", "createdBy"})
    List<RecurrencePlan> findAllByStatusOrderByNextEligibleDateAsc(RecurrencePlanStatus status);
}
