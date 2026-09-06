ALTER TABLE interest_lists
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN status_changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE bookings
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN cancelled_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE trips
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN assigned_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN status_changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE bookings DROP CONSTRAINT IF EXISTS bookings_booking_status_check;
ALTER TABLE bookings
    ADD CONSTRAINT bookings_booking_status_check
    CHECK (booking_status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'));

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(80) NOT NULL,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id VARCHAR(80) NOT NULL,
    actor_id BIGINT,
    actor_role VARCHAR(30),
    correlation_id UUID NOT NULL,
    payload TEXT NOT NULL,
    deduplication_key VARCHAR(180) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    next_attempt_at TIMESTAMP WITH TIME ZONE,
    processed_at TIMESTAMP WITH TIME ZONE,
    last_error TEXT,
    CONSTRAINT outbox_events_status_check CHECK (status IN ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED')),
    CONSTRAINT outbox_events_actor_fk FOREIGN KEY (actor_id) REFERENCES users (id)
);

CREATE INDEX idx_outbox_events_processing
    ON outbox_events (status, next_attempt_at, occurred_at);

CREATE INDEX idx_outbox_events_aggregate
    ON outbox_events (aggregate_type, aggregate_id, occurred_at);

CREATE INDEX idx_outbox_events_actor
    ON outbox_events (actor_id, occurred_at DESC);

CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    actor_id BIGINT,
    actor_role VARCHAR(30),
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id VARCHAR(80) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    correlation_id UUID NOT NULL,
    previous_state TEXT,
    resulting_state TEXT,
    metadata TEXT,
    CONSTRAINT audit_events_actor_fk
        FOREIGN KEY (actor_id) REFERENCES users (id)
);

CREATE INDEX idx_audit_events_entity
    ON audit_events (entity_type, entity_id, occurred_at DESC);

CREATE INDEX idx_audit_events_actor
    ON audit_events (actor_id, occurred_at DESC);

CREATE INDEX idx_audit_events_occurred_at
    ON audit_events (occurred_at DESC);

CREATE INDEX idx_interest_lists_reference_status
    ON interest_lists (reference_date, list_status);

CREATE INDEX idx_bookings_interest_status
    ON bookings (interest_list_id, booking_status);

CREATE INDEX idx_bookings_student_status
    ON bookings (student_id, booking_status);

CREATE INDEX idx_trips_status_assignment
    ON trips (status, conductor_id, vehicle_id);

CREATE TABLE system_metadata (
    metadata_key VARCHAR(100) PRIMARY KEY,
    metadata_value TEXT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO system_metadata (metadata_key, metadata_value)
VALUES ('operational_metrics_complete_since', CURRENT_TIMESTAMP::TEXT);
