package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
