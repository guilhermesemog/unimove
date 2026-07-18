package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findAllByConductor_Id(Long id);
}
