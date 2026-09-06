package com.guilhermesemog.unimove.service;

import java.util.UUID;
import com.guilhermesemog.unimove.exception.type.CpfAlreadyExistsException;
import com.guilhermesemog.unimove.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserValidationService {

    private final UserRepository userRepository;

    public UserValidationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validateCpf(UUID userId, String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return;
        }

        if (userRepository.existsByCpfAndIdNot(cpf, userId)) {
            throw new CpfAlreadyExistsException("CPF already exists: " + cpf);
        }
    }
}