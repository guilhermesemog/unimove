package com.guilhermesemog.unimove.dto.interestlist;

import com.guilhermesemog.unimove.model.enums.ListStatus;

import java.time.LocalDateTime;

public record InterestListPatch(
        LocalDateTime referenceDate,
        LocalDateTime closingDate,
        ListStatus listStatus
) {
}
