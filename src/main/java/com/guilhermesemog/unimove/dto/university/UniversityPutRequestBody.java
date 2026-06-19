package com.guilhermesemog.unimove.dto.university;

import jakarta.validation.constraints.NotBlank;

public record UniversityPutRequestBody(
        @NotBlank String name,
        @NotBlank String address
) {
}
