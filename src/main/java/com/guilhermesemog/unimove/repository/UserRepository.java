package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
