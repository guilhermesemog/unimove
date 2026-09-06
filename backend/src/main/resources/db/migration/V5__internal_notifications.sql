ALTER TABLE outbox_events
    ADD COLUMN locked_at TIMESTAMP WITH TIME ZONE;

UPDATE outbox_events SET locked_at = occurred_at WHERE status = 'PROCESSING';

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID NOT NULL,
    notification_type VARCHAR(40) NOT NULL,
    category VARCHAR(30) NOT NULL,
    content_key VARCHAR(100) NOT NULL,
    payload TEXT,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(500) NOT NULL,
    route VARCHAR(300),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP WITH TIME ZONE,
    deduplication_key VARCHAR(220) NOT NULL UNIQUE,
    CONSTRAINT notifications_recipient_fk FOREIGN KEY (recipient_id) REFERENCES users (id),
    CONSTRAINT notifications_type_check CHECK (notification_type IN ('BOOKING', 'DEMAND', 'TRIP', 'ASSIGNMENT', 'REMINDER', 'ISSUE')),
    CONSTRAINT notifications_category_check CHECK (category IN ('INFO', 'ACTION', 'REMINDER', 'ISSUE'))
);

CREATE INDEX idx_notifications_recipient_created
    ON notifications (recipient_id, created_at DESC);

CREATE INDEX idx_notifications_recipient_unread
    ON notifications (recipient_id, created_at DESC) WHERE read_at IS NULL;

CREATE INDEX idx_outbox_events_recovery
    ON outbox_events (status, locked_at, next_attempt_at, occurred_at);
