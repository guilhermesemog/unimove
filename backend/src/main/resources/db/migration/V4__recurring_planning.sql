CREATE TABLE recurrence_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    destination_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    closing_time TIME NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    return_departure_time TIME NOT NULL,
    return_arrival_time TIME NOT NULL,
    horizon_weeks INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL,
    next_eligible_date DATE,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT recurrence_plans_destination_fk FOREIGN KEY (destination_id) REFERENCES universities (id),
    CONSTRAINT recurrence_plans_created_by_fk FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT recurrence_plans_status_check CHECK (status IN ('ACTIVE', 'PAUSED', 'ARCHIVED')),
    CONSTRAINT recurrence_plans_dates_check CHECK (end_date >= start_date),
    CONSTRAINT recurrence_plans_horizon_check CHECK (horizon_weeks BETWEEN 1 AND 52)
);

CREATE TABLE recurrence_plan_days (
    recurrence_plan_id UUID NOT NULL,
    day_of_week VARCHAR(12) NOT NULL,
    PRIMARY KEY (recurrence_plan_id, day_of_week),
    CONSTRAINT recurrence_plan_days_plan_fk FOREIGN KEY (recurrence_plan_id)
        REFERENCES recurrence_plans (id) ON DELETE CASCADE,
    CONSTRAINT recurrence_plan_days_value_check CHECK (
        day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')
    )
);

ALTER TABLE interest_lists
    ADD COLUMN recurrence_plan_id UUID,
    ADD COLUMN occurrence_date DATE,
    ADD CONSTRAINT interest_lists_recurrence_plan_fk
        FOREIGN KEY (recurrence_plan_id) REFERENCES recurrence_plans (id),
    ADD CONSTRAINT interest_lists_occurrence_pair_check CHECK (
        (recurrence_plan_id IS NULL AND occurrence_date IS NULL)
        OR (recurrence_plan_id IS NOT NULL AND occurrence_date IS NOT NULL)
    ),
    ADD CONSTRAINT interest_lists_recurrence_occurrence_uk UNIQUE (recurrence_plan_id, occurrence_date);

CREATE INDEX idx_recurrence_plans_generation
    ON recurrence_plans (status, next_eligible_date, end_date);

CREATE INDEX idx_interest_lists_destination_date
    ON interest_lists (destination_id, reference_date);
