package com.guilhermesemog.unimove.audit;

import java.util.UUID;

public final class CorrelationIdContext {

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private CorrelationIdContext() {
    }

    public static void set(UUID correlationId) {
        CURRENT.set(correlationId);
    }

    public static UUID currentOrCreate() {
        UUID correlationId = CURRENT.get();
        if (correlationId == null) {
            correlationId = UUID.randomUUID();
            CURRENT.set(correlationId);
        }
        return correlationId;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
