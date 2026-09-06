package com.guilhermesemog.unimove.dto.university;

import java.util.UUID;
public record UniversityResponse(
        UUID id,
        String name,
        String address
) {
}
