
CREATE UNIQUE INDEX IF NOT EXISTS uq_swd_stadium_day
    ON stadium_working_days (stadium_id, day_of_week);


CREATE INDEX IF NOT EXISTS idx_event_bookings_field_time
    ON event_bookings (field_id, event_start, event_end);


