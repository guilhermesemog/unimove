package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, long id);

    @Query("SELECT u FROM User u WHERE LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :fullName, '%'))")
    Page<User> findAllByFullName(@Param("fullName") String fullName, Pageable pageable);
}
