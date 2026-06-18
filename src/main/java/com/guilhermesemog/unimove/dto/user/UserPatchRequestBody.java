package com.guilhermesemog.unimove.dto.user;

import jakarta.validation.constraints.Size;

public record UserPatchRequestBody(

        @Size(min = 11, max = 11, message = "CPF must be exactly 11 characters") String cpf,
        String firstName,
        String lastName,
        @Size(min = 8, max = 12, message = "Phone number must be between 8 and 12 characters") String phone

) {
}
