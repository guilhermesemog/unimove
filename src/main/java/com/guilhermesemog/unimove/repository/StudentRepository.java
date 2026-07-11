package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUser_Cpf(String cpf);
}
