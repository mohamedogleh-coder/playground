CREATE OR REPLACE FUNCTION check_booking_stadium_working_days_fn()
    RETURNS TRIGGER AS
$$
DECLARE
v_extra_time SMALLINT;
    v_day_of_week INT;
    v_open_time TIME;
    v_close_time TIME;
BEGIN
    -- get weekday
    v_day_of_week := EXTRACT(ISODOW FROM NEW.event_start);

    -- get working hours + extra time
SELECT s.extra_time, swd.opening_time, swd.closing_time
INTO v_extra_time, v_open_time, v_close_time
FROM stadium_working_days swd
         JOIN fields f ON f.stadium_id = swd.stadium_id
         JOIN stadiums s ON s.id = f.stadium_id
WHERE f.id = NEW.field_id
  AND swd.day_of_week = v_day_of_week;

-- check if open
IF v_open_time IS NULL THEN
        RAISE EXCEPTION 'Stadium is closed on this day';
END IF;

    -- check working hours
    IF (NEW.event_start::TIME < v_open_time
        OR NEW.event_start::TIME > v_close_time) THEN
        RAISE EXCEPTION 'Booking time is outside working hours';
END IF;

     NEW.event_end :=
            NEW.event_start
                + INTERVAL '1 hour'
                + (v_extra_time || ' minutes')::INTERVAL;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_booking_working_hours
    BEFORE INSERT OR UPDATE ON event_bookings
                         FOR EACH ROW
                         EXECUTE FUNCTION check_booking_stadium_working_days_fn();


