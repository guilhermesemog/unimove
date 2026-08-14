package com.guilhermesemog.unimove.dto.student;

import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopResponse;
import com.guilhermesemog.unimove.dto.university.UniversityResponse;
import com.guilhermesemog.unimove.dto.user.UserResponse;

public record StudentResponse(
        UserResponse user,
        Long period,
        String course,
        String address,
        UniversityResponse university,
        BoardingStopResponse preferredBoardingStop
) {
}
