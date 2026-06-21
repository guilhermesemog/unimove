package com.guilhermesemog.unimove.dto.conductor;

import com.guilhermesemog.unimove.dto.common.CommonUserCreate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ConductorCreate(
        @Valid CommonUserCreate user,
        @NotBlank(message = "License is required") String license,
        @NotNull(message = "License expiration date is required") LocalDate licenseExpirationDate
) {
}
