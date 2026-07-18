package com.guilhermesemog.unimove.dto.conductor;

import com.guilhermesemog.unimove.dto.user.UserResponse;

import java.time.LocalDate;

public record ConductorResponse(
        UserResponse user,
        String license,
        LocalDate licenseExpirationDate
) {
}
