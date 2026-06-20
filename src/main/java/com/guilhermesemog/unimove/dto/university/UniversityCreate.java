package com.guilhermesemog.unimove.dto.university;

import jakarta.validation.constraints.NotBlank;

public record UniversityCreate(
        @NotBlank String name,
        @NotBlank String address
) {
}
