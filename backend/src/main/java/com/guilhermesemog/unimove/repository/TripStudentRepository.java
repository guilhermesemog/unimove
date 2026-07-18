package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.TripStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripStudentRepository extends JpaRepository<TripStudent, Long> {
    List<TripStudent> findAllByStudent_Id(Long studentId);
}
