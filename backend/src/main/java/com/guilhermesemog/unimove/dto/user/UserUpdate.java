package com.guilhermesemog.unimove.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdate(

        @NotBlank(message = "CPF is required")
        @Size(min = 11, max = 11, message = "CPF must be exactly 11 characters")
        String cpf,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Phone is required")
        @Size(min = 8, max = 12, message = "Phone number must be between 8 and 12 characters")
        String phone

) {
}
