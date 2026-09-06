package com.guilhermesemog.unimove.repository;

import java.util.UUID;
import com.guilhermesemog.unimove.model.TripStudent;
import com.guilhermesemog.unimove.model.embeddable.TripStudentId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripStudentRepository extends JpaRepository<TripStudent, TripStudentId> {
    Page<TripStudent> findAllByStudent_Id(UUID studentId, Pageable pageable);

    Page<TripStudent> findAllByTrip_Id(UUID tripId, Pageable pageable);
}
