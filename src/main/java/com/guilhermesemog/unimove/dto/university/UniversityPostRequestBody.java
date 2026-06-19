package com.guilhermesemog.unimove.dto.university;

import jakarta.validation.constraints.NotBlank;

public record UniversityPostRequestBody(
        @NotBlank String name,
        @NotBlank String address
) {
}
