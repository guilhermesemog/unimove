package com.guilhermesemog.unimove.dto.audit;

public record AuditChangeResponse(String field, String label, String previousValue, String resultingValue) {
}
