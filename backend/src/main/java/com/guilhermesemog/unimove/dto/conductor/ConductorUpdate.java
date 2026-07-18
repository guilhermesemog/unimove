package com.guilhermesemog.unimove.dto.conductor;

import com.guilhermesemog.unimove.dto.user.UserUpdate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ConductorUpdate(
        @Valid UserUpdate user,
        @NotBlank(message = "License is required") String license,
        @NotNull(message = "License expiration date is required") LocalDate licenseExpirationDate
) {
}
