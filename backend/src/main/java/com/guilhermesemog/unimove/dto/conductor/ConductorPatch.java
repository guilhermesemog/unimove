package com.guilhermesemog.unimove.dto.conductor;

import com.guilhermesemog.unimove.dto.user.UserPatch;
import jakarta.validation.Valid;

import java.time.LocalDate;

public record ConductorPatch(
        @Valid UserPatch user,
        String license,
        LocalDate licenseExpirationDate
) {
}
