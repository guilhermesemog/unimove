package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByPlate(String plate);

    Page<Vehicle> findAllByPlateContainsIgnoreCase(String plate, Pageable pageable);
}
