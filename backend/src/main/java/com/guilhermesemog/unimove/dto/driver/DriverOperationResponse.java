package com.guilhermesemog.unimove.dto.driver;

import com.guilhermesemog.unimove.dto.trip.TripResponse;

import java.util.List;

public record DriverOperationResponse(
        TripResponse trip,
        List<DriverManifestPassengerResponse> passengers
) {
}
