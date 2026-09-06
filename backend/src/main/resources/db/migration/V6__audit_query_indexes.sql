CREATE INDEX idx_audit_events_action_occurred
    ON audit_events (action, occurred_at DESC);

CREATE INDEX idx_audit_events_correlation
    ON audit_events (correlation_id, occurred_at ASC);
