package com.guilhermesemog.unimove.dto.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserBody(

        @NotBlank(message = "CPF is required")
        @Size(min = 11, max = 11, message = "CPF must be exactly 11 characters")
        String cpf,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @NotBlank(message = "Phone is required")
        @Size(min = 8, max = 12, message = "Phone number must be between 8 and 12 characters")
        String phone,

        Boolean active

) {
}
