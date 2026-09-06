package com.guilhermesemog.unimove.dto.recurrence;

import java.time.LocalDate;
import java.util.List;

public record RecurrencePreviewResponse(
        LocalDate rangeStart,
        LocalDate rangeEnd,
        int createCount,
        int skipCount,
        int conflictCount,
        List<RecurrenceOccurrenceResponse> occurrences
) {
}
