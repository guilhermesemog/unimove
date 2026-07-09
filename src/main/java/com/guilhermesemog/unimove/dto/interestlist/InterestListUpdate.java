package com.guilhermesemog.unimove.dto.interestlist;

import com.guilhermesemog.unimove.model.enums.ListStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record InterestListUpdate(
        @NotNull(message = "Reference date is required") LocalDateTime referenceDate,
        @NotNull(message = "Closing date is required") LocalDateTime closingDate,
        @NotNull(message = "List status is required") ListStatus listStatus
) {
}
