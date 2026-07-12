
DROP TRIGGER IF EXISTS trg_check_booking_working_hours ON event_bookings;
DROP FUNCTION IF EXISTS check_booking_stadium_working_days_fn();

ALTER TABLE event_bookings
    ADD COLUMN IF NOT EXISTS extra_time smallint NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS duration   interval NOT NULL DEFAULT INTERVAL '1 hour';


CREATE OR REPLACE FUNCTION fn_check_before_event_booking()
    RETURNS TRIGGER AS
$$
DECLARE
v_extra_time  SMALLINT;
    v_day_of_week INT;
    v_open_time   TIME;
    v_close_time  TIME;
BEGIN
    -- dhererka booking-ka - haddii aan la gudbin, default 1 saac
    NEW.duration := COALESCE(NEW.duration, INTERVAL '1 hour');

    IF NEW.duration <= INTERVAL '0' THEN
        RAISE EXCEPTION 'Booking duration must be greater than zero';
END IF;

    v_day_of_week := EXTRACT(ISODOW FROM NEW.event_start);

    -- saacadaha shaqada + extra_time-ga garoonka maalintaas
SELECT s.extra_time, swd.opening_time, swd.closing_time
INTO v_extra_time, v_open_time, v_close_time
FROM stadium_working_days swd
         JOIN fields f ON f.stadium_id = swd.stadium_id
         JOIN stadiums s ON s.id = f.stadium_id
WHERE f.id = NEW.field_id
  AND swd.day_of_week = v_day_of_week;

IF v_open_time IS NULL THEN
        RAISE EXCEPTION 'Stadium is closed on this day';
END IF;

    -- event_end = start + duration KELIYA (extra_time lagama darsan)
    NEW.event_end := NEW.event_start + NEW.duration;

    -- extra_time-ga la keydiyo waa mid stadium-ka wakhtigaas ah;
    -- EXCLUDE constraint-ka ayaa ku isticmaali doona sida buffer-ka
    NEW.extra_time := COALESCE(v_extra_time, 0);

    -- hubi bilowga iyo dhammaadka labadaba inay ku jiraan saacadaha shaqada
    IF NEW.event_start::TIME < v_open_time
        OR NEW.event_end::TIME > v_close_time THEN
        RAISE EXCEPTION 'Booking time is outside working hours';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_check_before_event_booking
    BEFORE INSERT OR UPDATE ON event_bookings
                         FOR EACH ROW
                         EXECUTE FUNCTION fn_check_before_event_booking();

