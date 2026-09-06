CREATE TABLE product_telemetry_events (
    id UUID PRIMARY KEY,
    event_name VARCHAR(60) NOT NULL,
    actor_role VARCHAR(30) NOT NULL,
    session_id UUID NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    properties JSONB NOT NULL DEFAULT '{}'::jsonb,
    CONSTRAINT product_telemetry_role_check CHECK (actor_role IN ('ADMIN', 'STUDENT', 'CONDUCTOR')),
    CONSTRAINT product_telemetry_event_check CHECK (event_name IN (
        'availability_viewed', 'booking_started', 'booking_step_completed',
        'booking_validation_failed', 'booking_completed', 'booking_abandoned', 'booking_cancelled',
        'recurrence_started', 'recurrence_previewed', 'recurrence_conflict_found',
        'recurrence_published', 'trip_generated', 'assignment_completed',
        'operation_opened', 'manifest_viewed', 'operation_load_failed'
    ))
);

CREATE INDEX idx_product_telemetry_received
    ON product_telemetry_events (received_at);

CREATE INDEX idx_product_telemetry_event_occurred
    ON product_telemetry_events (event_name, occurred_at);

CREATE INDEX idx_product_telemetry_role_occurred
    ON product_telemetry_events (actor_role, occurred_at);
