package com.guilhermesemog.unimove.repository;

import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.guilhermesemog.unimove.model.InterestList;
import com.guilhermesemog.unimove.model.enums.ListStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestListRepository extends JpaRepository<InterestList, UUID> {

    List<InterestList> findAllByDestination_IdAndReferenceDateBetween(UUID destinationId, LocalDate from, LocalDate to);

    long countByRecurrencePlan_IdAndReferenceDateGreaterThanEqual(UUID recurrencePlanId, LocalDate date);

    List<InterestList> findAllByListStatusAndReferenceDateBetween(ListStatus status, LocalDate from, LocalDate to);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = """
            INSERT INTO interest_lists (
                id, reference_date, closing_time, departure_time, arrival_time,
                return_departure_time, return_arrival_time, destination_id, list_status,
                created_at, updated_at, version, status_changed_at, recurrence_plan_id, occurrence_date
            ) VALUES (
                :id, :occurrenceDate, :closingTime, :departureTime, :arrivalTime,
                :returnDepartureTime, :returnArrivalTime, :destinationId, 'OPEN',
                :now, :now, 0, :now, :planId, :occurrenceDate
            ) ON CONFLICT (recurrence_plan_id, occurrence_date) DO NOTHING
            """, nativeQuery = true)
    int insertGeneratedOccurrence(
            UUID id,
            LocalDate occurrenceDate,
            LocalTime closingTime,
            LocalTime departureTime,
            LocalTime arrivalTime,
            LocalTime returnDepartureTime,
            LocalTime returnArrivalTime,
            UUID destinationId,
            Instant now,
            UUID planId
    );
}
