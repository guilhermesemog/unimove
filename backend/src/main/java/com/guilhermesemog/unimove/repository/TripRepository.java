package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    Page<Trip> findAllByConductor_Id(Long id, Pageable pageable);

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

    boolean existsByInterestList_Id(Long id);

}
