-- Convert legacy numeric identifiers without discarding existing records.
LOCK TABLE universities, boarding_stops, users, vehicles, conductors, students,
    interest_lists, bookings, trips, trip_students, trip_interest_lists,
    trip_boarding_points, trip_stop_points, outbox_events, audit_events
    IN ACCESS EXCLUSIVE MODE;

CREATE FUNCTION unimove_legacy_id_to_uuid(value BIGINT)
RETURNS UUID LANGUAGE SQL IMMUTABLE STRICT
AS $$ SELECT md5('unimove-legacy-id:' || value::TEXT)::UUID $$;

CREATE FUNCTION unimove_legacy_text_id_to_uuid(value TEXT)
RETURNS UUID LANGUAGE plpgsql IMMUTABLE STRICT
AS $$
BEGIN
    IF value !~ '^[0-9]+$' THEN
        RAISE EXCEPTION 'Cannot convert legacy identifier "%" to UUID', value;
    END IF;
    RETURN unimove_legacy_id_to_uuid(value::BIGINT);
END
$$;

-- Hibernate-generated constraint names can differ from the names in V1.
DO $$
DECLARE
    foreign_key RECORD;
BEGIN
    FOR foreign_key IN
        SELECT conrelid::regclass AS table_name, conname
        FROM pg_constraint
        WHERE contype = 'f'
          AND conrelid IN (
              'conductors'::regclass, 'students'::regclass, 'interest_lists'::regclass,
              'bookings'::regclass, 'trips'::regclass, 'trip_students'::regclass,
              'trip_interest_lists'::regclass, 'trip_boarding_points'::regclass,
              'trip_stop_points'::regclass, 'outbox_events'::regclass,
              'audit_events'::regclass
          )
    LOOP
        EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', foreign_key.table_name, foreign_key.conname);
    END LOOP;
END
$$;

ALTER TABLE universities ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE universities ALTER COLUMN id DROP DEFAULT;
ALTER TABLE universities ALTER COLUMN id TYPE UUID USING unimove_legacy_id_to_uuid(id);
ALTER TABLE universities ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE boarding_stops ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE boarding_stops ALTER COLUMN id DROP DEFAULT;
ALTER TABLE boarding_stops ALTER COLUMN id TYPE UUID USING unimove_legacy_id_to_uuid(id);
ALTER TABLE boarding_stops ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE users ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE users ALTER COLUMN id DROP DEFAULT;
ALTER TABLE users ALTER COLUMN id TYPE UUID USING unimove_legacy_id_to_uuid(id);
ALTER TABLE users ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE vehicles ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE vehicles ALTER COLUMN id DROP DEFAULT;
ALTER TABLE vehicles ALTER COLUMN id TYPE UUID USING unimove_legacy_id_to_uuid(id);
ALTER TABLE vehicles ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE conductors ALTER COLUMN user_id TYPE UUID USING unimove_legacy_id_to_uuid(user_id);
ALTER TABLE students ALTER COLUMN user_id TYPE UUID USING unimove_legacy_id_to_uuid(user_id);
ALTER TABLE students ALTER COLUMN university_id TYPE UUID USING unimove_legacy_id_to_uuid(university_id);
ALTER TABLE students ALTER COLUMN preferred_boarding_stop_id TYPE UUID USING unimove_legacy_id_to_uuid(preferred_boarding_stop_id);

ALTER TABLE interest_lists ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE interest_lists ALTER COLUMN id DROP DEFAULT;
ALTER TABLE interest_lists ALTER COLUMN id TYPE UUID USING unimove_legacy_id_to_uuid(id);
ALTER TABLE interest_lists ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE interest_lists ALTER COLUMN destination_id TYPE UUID USING unimove_legacy_id_to_uuid(destination_id);

ALTER TABLE bookings ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE bookings ALTER COLUMN id DROP DEFAULT;
ALTER TABLE bookings ALTER COLUMN id TYPE UUID USING unimove_legacy_id_to_uuid(id);
ALTER TABLE bookings ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE bookings ALTER COLUMN student_id TYPE UUID USING unimove_legacy_id_to_uuid(student_id);
ALTER TABLE bookings ALTER COLUMN interest_list_id TYPE UUID USING unimove_legacy_id_to_uuid(interest_list_id);
ALTER TABLE bookings ALTER COLUMN destination_id TYPE UUID USING unimove_legacy_id_to_uuid(destination_id);
ALTER TABLE bookings ALTER COLUMN boarding_location_id TYPE UUID USING unimove_legacy_id_to_uuid(boarding_location_id);

ALTER TABLE trips ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE trips ALTER COLUMN id DROP DEFAULT;
ALTER TABLE trips ALTER COLUMN id TYPE UUID USING unimove_legacy_id_to_uuid(id);
ALTER TABLE trips ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE trips ALTER COLUMN interest_list_id TYPE UUID USING unimove_legacy_id_to_uuid(interest_list_id);
ALTER TABLE trips ALTER COLUMN conductor_id TYPE UUID USING unimove_legacy_id_to_uuid(conductor_id);
ALTER TABLE trips ALTER COLUMN vehicle_id TYPE UUID USING unimove_legacy_id_to_uuid(vehicle_id);

ALTER TABLE trip_students ALTER COLUMN trip_id TYPE UUID USING unimove_legacy_id_to_uuid(trip_id);
ALTER TABLE trip_students ALTER COLUMN student_id TYPE UUID USING unimove_legacy_id_to_uuid(student_id);
ALTER TABLE trip_interest_lists ALTER COLUMN trip_id TYPE UUID USING unimove_legacy_id_to_uuid(trip_id);
ALTER TABLE trip_interest_lists ALTER COLUMN interest_list_id TYPE UUID USING unimove_legacy_id_to_uuid(interest_list_id);
ALTER TABLE trip_boarding_points ALTER COLUMN trip_id TYPE UUID USING unimove_legacy_id_to_uuid(trip_id);
ALTER TABLE trip_boarding_points ALTER COLUMN boarding_stop_id TYPE UUID USING unimove_legacy_id_to_uuid(boarding_stop_id);
ALTER TABLE trip_stop_points ALTER COLUMN trip_id TYPE UUID USING unimove_legacy_id_to_uuid(trip_id);
ALTER TABLE trip_stop_points ALTER COLUMN university_id TYPE UUID USING unimove_legacy_id_to_uuid(university_id);

ALTER TABLE audit_events ALTER COLUMN actor_id TYPE UUID USING unimove_legacy_id_to_uuid(actor_id);
ALTER TABLE audit_events ALTER COLUMN entity_id TYPE UUID USING unimove_legacy_text_id_to_uuid(entity_id);
ALTER TABLE outbox_events ALTER COLUMN actor_id TYPE UUID USING unimove_legacy_id_to_uuid(actor_id);
ALTER TABLE outbox_events ALTER COLUMN aggregate_id TYPE UUID USING unimove_legacy_text_id_to_uuid(aggregate_id);

ALTER TABLE conductors ADD CONSTRAINT fk_conductors_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE students ADD CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE students ADD CONSTRAINT fk_students_university FOREIGN KEY (university_id) REFERENCES universities(id);
ALTER TABLE students ADD CONSTRAINT fk_students_preferred_stop FOREIGN KEY (preferred_boarding_stop_id) REFERENCES boarding_stops(id);
ALTER TABLE interest_lists ADD CONSTRAINT fk_interest_lists_destination FOREIGN KEY (destination_id) REFERENCES universities(id);
ALTER TABLE bookings ADD CONSTRAINT fk_bookings_student FOREIGN KEY (student_id) REFERENCES students(user_id);
ALTER TABLE bookings ADD CONSTRAINT fk_bookings_interest_list FOREIGN KEY (interest_list_id) REFERENCES interest_lists(id);
ALTER TABLE bookings ADD CONSTRAINT fk_bookings_destination FOREIGN KEY (destination_id) REFERENCES universities(id);
ALTER TABLE bookings ADD CONSTRAINT fk_bookings_boarding_stop FOREIGN KEY (boarding_location_id) REFERENCES boarding_stops(id);
ALTER TABLE trips ADD CONSTRAINT fk_trips_interest_list FOREIGN KEY (interest_list_id) REFERENCES interest_lists(id);
ALTER TABLE trips ADD CONSTRAINT fk_trips_conductor FOREIGN KEY (conductor_id) REFERENCES conductors(user_id);
ALTER TABLE trips ADD CONSTRAINT fk_trips_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id);
ALTER TABLE trip_students ADD CONSTRAINT fk_trip_students_trip FOREIGN KEY (trip_id) REFERENCES trips(id);
ALTER TABLE trip_students ADD CONSTRAINT fk_trip_students_student FOREIGN KEY (student_id) REFERENCES students(user_id);
ALTER TABLE trip_interest_lists ADD CONSTRAINT fk_trip_interest_lists_trip FOREIGN KEY (trip_id) REFERENCES trips(id);
ALTER TABLE trip_interest_lists ADD CONSTRAINT fk_trip_interest_lists_interest_list FOREIGN KEY (interest_list_id) REFERENCES interest_lists(id);
ALTER TABLE trip_boarding_points ADD CONSTRAINT fk_trip_boarding_points_trip FOREIGN KEY (trip_id) REFERENCES trips(id);
ALTER TABLE trip_boarding_points ADD CONSTRAINT fk_trip_boarding_points_stop FOREIGN KEY (boarding_stop_id) REFERENCES boarding_stops(id);
ALTER TABLE trip_stop_points ADD CONSTRAINT fk_trip_stop_points_trip FOREIGN KEY (trip_id) REFERENCES trips(id);
ALTER TABLE trip_stop_points ADD CONSTRAINT fk_trip_stop_points_university FOREIGN KEY (university_id) REFERENCES universities(id);
ALTER TABLE outbox_events ADD CONSTRAINT outbox_events_actor_fk FOREIGN KEY (actor_id) REFERENCES users(id);
ALTER TABLE audit_events ADD CONSTRAINT audit_events_actor_fk FOREIGN KEY (actor_id) REFERENCES users(id);

DROP FUNCTION unimove_legacy_text_id_to_uuid(TEXT);
DROP FUNCTION unimove_legacy_id_to_uuid(BIGINT);
