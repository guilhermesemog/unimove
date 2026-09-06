package com.guilhermesemog.unimove.repository;

import java.util.UUID;
import com.guilhermesemog.unimove.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<Student> findByUser_Cpf(String cpf);

    boolean existsByUser_Cpf(String userCpf);
}
