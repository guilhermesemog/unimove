package com.guilhermesemog.unimove.dto.booking;

import com.guilhermesemog.unimove.model.enums.BookingStatus;
import com.guilhermesemog.unimove.model.enums.TripType;

public record BookingResponse(
        Long id,
        TripType tripType,
        BookingStatus bookingStatus,
        Long studentId,
        Long interestListId,
        Long destinationId,
        Long boardingLocationId
) {
}
