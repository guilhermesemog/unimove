CREATE INDEX idx_bookings_created_status
    ON bookings (created_at, booking_status);

CREATE INDEX idx_bookings_cancelled_at
    ON bookings (cancelled_at) WHERE cancelled_at IS NOT NULL;

CREATE INDEX idx_trips_created_at
    ON trips (created_at);

CREATE INDEX idx_trips_assigned_at
    ON trips (assigned_at) WHERE assigned_at IS NOT NULL;

CREATE INDEX idx_interest_lists_recurrence_reference
    ON interest_lists (reference_date, recurrence_plan_id);
