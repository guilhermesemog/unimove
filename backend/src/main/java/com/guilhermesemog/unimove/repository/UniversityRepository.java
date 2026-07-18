package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UniversityRepository extends JpaRepository<University, Long> {
    public Optional<University> findByName(String name);
}
