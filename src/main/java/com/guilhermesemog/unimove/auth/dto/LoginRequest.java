package com.guilhermesemog.unimove.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(min = 11, max = 11, message = "CPF must have 11 characters") String cpf,
        @NotBlank String password
) {
}
