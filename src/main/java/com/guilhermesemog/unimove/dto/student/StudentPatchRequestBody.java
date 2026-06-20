package com.guilhermesemog.unimove.dto.student;

import com.guilhermesemog.unimove.dto.user.UserPatchRequestBody;
import jakarta.validation.Valid;

public record StudentPatchRequestBody(
        @Valid UserPatchRequestBody user,
        Long period,
        String course,
        String address,
        Long universityId,
        Long preferredBoardingStopId
) {
}
