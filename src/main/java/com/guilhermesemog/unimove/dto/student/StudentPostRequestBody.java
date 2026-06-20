package com.guilhermesemog.unimove.dto.student;

import com.guilhermesemog.unimove.dto.common.CreateUserBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentPostRequestBody(
        @Valid CreateUserBody user,
        @NotNull(message = "Period is required") Long period,
        @NotBlank(message = "Course is required") String course,
        @NotBlank(message = "Address is required") String address,
        @NotNull(message = "University ID is required") Long universityId,
        Long preferredBoardingStopId
) {
}
