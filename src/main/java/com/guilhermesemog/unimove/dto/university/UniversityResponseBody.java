package com.guilhermesemog.unimove.dto.university;

import jakarta.validation.constraints.NotBlank;

public record UniversityResponseBody(
        Long id,
        String name,
        String address
) {
}
