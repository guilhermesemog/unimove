package com.guilhermesemog.unimove.dto.booking;

import java.util.UUID;
import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopResponse;
import com.guilhermesemog.unimove.dto.interestlist.InterestListResponse;
import com.guilhermesemog.unimove.dto.student.StudentResponse;
import com.guilhermesemog.unimove.dto.university.UniversityResponse;
import com.guilhermesemog.unimove.model.enums.BookingStatus;
import com.guilhermesemog.unimove.model.enums.TripType;

public record BookingResponse(
        UUID id,
        TripType tripType,
        BookingStatus bookingStatus,
        StudentResponse student,
        InterestListResponse interestList,
        UniversityResponse destination,
        BoardingStopResponse boardingLocation
) {
}
