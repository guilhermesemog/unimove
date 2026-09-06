package com.guilhermesemog.unimove.dto.trip;

import java.util.UUID;
import com.guilhermesemog.unimove.dto.conductor.ConductorResponse;
import com.guilhermesemog.unimove.dto.interestlist.InterestListResponse;
import com.guilhermesemog.unimove.dto.vehicle.VehicleResponse;
import com.guilhermesemog.unimove.model.enums.TripStatus;

public record TripResponse(
        UUID id,
        TripStatus status,
        InterestListResponse interestList,
        ConductorResponse conductor,
        VehicleResponse vehicle
) {
}
