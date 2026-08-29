package com.guilhermesemog.unimove.dto.admin;

import com.guilhermesemog.unimove.dto.interestlist.InterestListResponse;
import com.guilhermesemog.unimove.dto.trip.TripResponse;

public record AdminOperationResponse(
        InterestListResponse demand,
        long bookingCount,
        TripResponse trip
) {
}
