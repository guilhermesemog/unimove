package com.guilhermesemog.unimove.dto.student;

import com.guilhermesemog.unimove.dto.user.UserPutRequestBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentPutRequestBody(
        @Valid UserPutRequestBody user,
        @NotNull(message = "Period is required") Long period,
        @NotBlank(message = "Course is required") String course,
        @NotBlank(message = "Address is required") String address,
        @NotNull(message = "University ID is required") Long universityId,
        Long preferredBoardingStopId
) {
}
