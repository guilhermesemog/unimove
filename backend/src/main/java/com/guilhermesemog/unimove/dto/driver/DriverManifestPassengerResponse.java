package com.guilhermesemog.unimove.dto.driver;

import java.util.UUID;
import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopResponse;
import com.guilhermesemog.unimove.model.enums.TripType;

public record DriverManifestPassengerResponse(
        UUID bookingId,
        String firstName,
        String lastName,
        String phone,
        BoardingStopResponse boardingStop,
        TripType tripType
) {
}
