DROP TRIGGER IF EXISTS trg_check_before_event_booking ON event_bookings;

DROP FUNCTION IF EXISTS check_booking_stadium_working_days_fn;


CREATE OR REPLACE FUNCTION check_booking_stadium_working_days_fn()
    RETURNS TRIGGER AS
$$
DECLARE
    v_extra_time            SMALLINT;
    v_day_of_week           INT;
    v_open_time             TIME;
    v_close_time            TIME;
    v_half_booking          BOOLEAN;
    v_stop_booking          BOOLEAN;
    v_is_open               BOOLEAN;
    v_slot_duration_minutes INT;
    v_minutes_since_open    NUMERIC;
BEGIN
    v_day_of_week := EXTRACT(ISODOW FROM NEW.event_start);

    -- Si tallaabo hal-tag ah (single index lookup) loo helo, waxaan ka bilaabaynaa fields (PK)
    SELECT s.extra_time,
           swd.opening_time,
           swd.closing_time,
           s.half_booking,
           swd.is_open,
           f.stop_booking
    INTO v_extra_time,
        v_open_time,
        v_close_time,
        v_half_booking,
        v_is_open,
        v_stop_booking
    FROM fields f
             JOIN stadiums s ON s.id = f.stadium_id
             JOIN stadium_working_days swd
                  ON swd.stadium_id = f.stadium_id
                      AND swd.day_of_week = v_day_of_week
    WHERE f.id = NEW.field_id;

    IF NOT FOUND OR v_is_open IS NOT TRUE OR v_open_time IS NULL THEN
        RAISE EXCEPTION 'Fadlan malinka aad doratay garoonku ma shaqaynayo' USING ERRCODE = '45000';
    END IF;

    IF v_stop_booking IS TRUE THEN
        RAISE EXCEPTION 'Fadlan playground-kan adminka ayaa booking ka joojey' USING ERRCODE = '45000';
    END IF;

    IF v_half_booking IS NOT TRUE AND NEW.event_status = 'half' THEN
        RAISE EXCEPTION 'Fadlan garoonkan ma ogola half booking waxa la aqabalayaa in hal mar laysku qaato event-ka' USING ERRCODE = '45000';
    END IF;

    IF NEW.event_start::TIME < v_open_time OR NEW.event_start::TIME > v_close_time THEN
        RAISE EXCEPTION 'Fadlan % garoonku ma shaqeeyo', NEW.event_start USING ERRCODE = '45000';
    END IF;

    v_slot_duration_minutes := 60 + v_extra_time;
    v_minutes_since_open := EXTRACT(EPOCH FROM (NEW.event_start::TIME - v_open_time)) / 60;

    IF MOD(v_minutes_since_open, v_slot_duration_minutes::NUMERIC) <> 0 THEN
        RAISE EXCEPTION 'Fadlan % ka mid maha xiliyada garoonkan laga heli karo', NEW.event_start USING ERRCODE = '45000';
    END IF;

    NEW.extra_time := v_extra_time;
    NEW.event_end := NEW.event_start + make_interval(mins => v_slot_duration_minutes);

    IF EXISTS (SELECT 1
               FROM event_bookings eb
               WHERE eb.field_id = NEW.field_id
                 AND eb.id IS DISTINCT FROM NEW.id
                 AND eb.event_start < NEW.event_end
                 AND eb.event_end > NEW.event_start) THEN
        RAISE EXCEPTION 'Xiliga % ee aad qabsatay ma aha mid available ah', NEW.event_start  USING ERRCODE = '45000';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger-ka: ha ku shaqayn UPDATE-yada aan wax saameyn ku yeelin xogta muhiimka ah
CREATE TRIGGER trg_check_before_event_booking
    BEFORE INSERT OR UPDATE OF event_start, field_id, event_status
    ON event_bookings
    FOR EACH ROW
    WHEN (
        pg_trigger_depth() = 0
        )
EXECUTE FUNCTION check_booking_stadium_working_days_fn();

