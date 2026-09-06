package com.guilhermesemog.unimove.dto.student;

import java.util.UUID;
import com.guilhermesemog.unimove.dto.common.CommonUserCreate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentCreate(
        @Valid CommonUserCreate user,
        @NotNull(message = "Period is required") Long period,
        @NotBlank(message = "Course is required") String course,
        @NotBlank(message = "Address is required") String address,
        @NotNull(message = "University ID is required") UUID universityId,
        UUID preferredBoardingStopId
) {
}
