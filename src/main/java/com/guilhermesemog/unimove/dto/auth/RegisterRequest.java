package com.guilhermesemog.unimove.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 11, max = 11, message = "CPF must have 11 characters") String cpf,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName
) {
}
