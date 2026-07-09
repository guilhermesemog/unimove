package com.guilhermesemog.unimove.dto.interestlist;

import java.time.LocalDateTime;

public record InterestListCreate(
        LocalDateTime referenceDate,
        LocalDateTime closingDate
) {
}
