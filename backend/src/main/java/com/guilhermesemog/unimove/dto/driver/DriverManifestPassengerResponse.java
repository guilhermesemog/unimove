package com.guilhermesemog.unimove.dto.driver;

import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopResponse;
import com.guilhermesemog.unimove.model.enums.TripType;

public record DriverManifestPassengerResponse(
        Long bookingId,
        String firstName,
        String lastName,
        String phone,
        BoardingStopResponse boardingStop,
        TripType tripType
) {
}
