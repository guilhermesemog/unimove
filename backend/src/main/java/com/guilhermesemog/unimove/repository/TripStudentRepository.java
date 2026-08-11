package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.TripStudent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripStudentRepository extends JpaRepository<TripStudent, Long> {
    Page<TripStudent> findAllByStudent_Id(Long studentId, Pageable pageable);

    Page<TripStudent> findAllByTrip_Id(Long tripId, Pageable pageable);
}
