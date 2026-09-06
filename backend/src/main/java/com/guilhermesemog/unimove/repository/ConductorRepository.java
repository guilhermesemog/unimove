package com.guilhermesemog.unimove.repository;

import java.util.UUID;
import com.guilhermesemog.unimove.model.Conductor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConductorRepository extends JpaRepository<Conductor, UUID> {
    Optional<Conductor> findByUser_Cpf(String cpf);
}
