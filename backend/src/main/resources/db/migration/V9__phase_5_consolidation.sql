-- The telemetry summary filters the whole event stream by period before aggregating by name.
-- The V8 compound indexes serve role/name slices; this index serves the period-wide summary.
CREATE INDEX idx_product_telemetry_occurred
    ON product_telemetry_events (occurred_at);
