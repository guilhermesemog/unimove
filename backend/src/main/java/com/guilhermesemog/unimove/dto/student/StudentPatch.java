package com.guilhermesemog.unimove.dto.student;

import com.guilhermesemog.unimove.dto.user.UserPatch;
import jakarta.validation.Valid;

public record StudentPatch(
        @Valid UserPatch user,
        Long period,
        String course,
        String address,
        Long universityId,
        Long preferredBoardingStopId
) {
}
