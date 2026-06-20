package com.guilhermesemog.unimove.dto.student;

import com.guilhermesemog.unimove.dto.user.UserResponseBody;

public record StudentResponseBody(
        UserResponseBody user,
        Long period,
        String course,
        String address,
        String university,
        String preferredBoardingStop
) {
}
