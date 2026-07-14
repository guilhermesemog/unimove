package com.guilhermesemog.unimove.dto.trip;

import java.util.List;

public record TripCreate(
        List<Long> interestListIds
) {
}
