package com.guilhermesemog.unimove.dto.student;

import java.util.UUID;
import com.guilhermesemog.unimove.dto.user.UserPatch;
import jakarta.validation.Valid;

public record StudentPatch(
        @Valid UserPatch user,
        Long period,
        String course,
        String address,
        UUID universityId,
        UUID preferredBoardingStopId
) {
}
