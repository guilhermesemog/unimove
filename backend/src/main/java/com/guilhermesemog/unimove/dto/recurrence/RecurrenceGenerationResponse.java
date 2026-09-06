package com.guilhermesemog.unimove.dto.recurrence;

import java.util.List;
import java.util.UUID;

public record RecurrenceGenerationResponse(
        UUID planId,
        int createdCount,
        int skippedCount,
        int conflictCount,
        List<RecurrenceOccurrenceResponse> occurrences
) {
}
