package com.guilhermesemog.unimove.dto.student;

import com.guilhermesemog.unimove.dto.user.UserResponse;

public record StudentResponse(
        UserResponse user,
        Long period,
        String course,
        String address,
        String university,
        String preferredBoardingStop
) {
}
