package com.guilhermesemog.unimove.dto.recurrence;

import com.guilhermesemog.unimove.model.enums.RecurrencePlanStatus;
import jakarta.validation.constraints.NotNull;

public record RecurrenceStatusUpdate(@NotNull RecurrencePlanStatus status) {
}
