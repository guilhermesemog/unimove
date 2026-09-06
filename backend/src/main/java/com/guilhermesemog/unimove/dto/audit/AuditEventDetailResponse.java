package com.guilhermesemog.unimove.dto.audit;

import java.util.Map;

public record AuditEventDetailResponse(
        AuditEventResponse event,
        Map<String, Object> previousState,
        Map<String, Object> resultingState,
        Map<String, Object> metadata
) {
}
