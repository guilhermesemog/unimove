package com.guilhermesemog.unimove.repository;

import java.util.UUID;
import com.guilhermesemog.unimove.model.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, UUID> {
    Page<Trip> findAllByConductor_Id(UUID id, Pageable pageable);

    Optional<Trip> findByIdAndConductor_Id(UUID id, UUID conductorId);

    @Query("""
                SELECT t
                FROM Trip t
                WHERE
                    LOWER(CONCAT(t.conductor.user.firstName, ' ', t.conductor.user.lastName))
                        LIKE LOWER(CONCAT('%', :search, '%'))
                    OR
                    LOWER(t.vehicle.plate)
                        LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<Trip> findAllBySearch(
            @Param("search") String search,
            Pageable pageable
    );

    boolean existsByInterestList_Id(UUID id);

    Optional<Trip> findByInterestList_Id(UUID interestListId);

    List<Trip> findAllByInterestList_ReferenceDateBetween(LocalDate from, LocalDate to);

}
