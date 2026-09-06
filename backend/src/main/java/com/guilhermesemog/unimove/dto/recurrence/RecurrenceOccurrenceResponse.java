package com.guilhermesemog.unimove.dto.recurrence;

import com.guilhermesemog.unimove.model.enums.RecurrenceOccurrenceAction;

import java.time.LocalDate;
import java.util.UUID;

public record RecurrenceOccurrenceResponse(
        LocalDate date,
        RecurrenceOccurrenceAction action,
        UUID interestListId,
        String reason
) {
}
