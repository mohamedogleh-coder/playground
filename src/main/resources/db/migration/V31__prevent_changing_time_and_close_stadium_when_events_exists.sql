DROP TRIGGER IF EXISTS fn_check_working_day_conflicts ON stadium_working_days;
DROP FUNCTION IF EXISTS fn_check_working_day_conflicts;

CREATE
OR REPLACE FUNCTION fn_prevent_changing_time_and_close_stadium_when_events_exists()
    RETURNS TRIGGER AS
$$
DECLARE
v_booking_count integer;
    v_min_start_time
time;
    v_max_end_time
time;
BEGIN
    IF
NEW.is_open IS NOT DISTINCT FROM OLD.is_open
            AND NEW.opening_time IS NOT DISTINCT FROM OLD.opening_time
            AND NEW.closing_time IS NOT DISTINCT FROM OLD.closing_time THEN
        RETURN NEW;
END IF;

SELECT count(*)::int, min(e.event_start::time),
       max(e.event_end::time)
INTO
    v_booking_count,
    v_min_start_time,
    v_max_end_time
FROM event_bookings e
         JOIN fields f ON f.id = e.field_id
WHERE f.stadium_id = NEW.stadium_id
  AND extract(isodow FROM e.event_start) = NEW.day_of_week
  AND e.event_start >= now();

IF
v_booking_count = 0 THEN
        RETURN NEW;
END IF;

    IF
NEW.is_open = false THEN
        RAISE EXCEPTION 'Malinka % da waxa jira % booking sidaas darteed lama ogola in la xidho garoon-ka',
            get_day_name(NEW.day_of_week), v_booking_count
            USING ERRCODE = '45000';
END IF;

    IF
NEW.opening_time > v_min_start_time THEN
        RAISE EXCEPTION 'Malinka % da waxa jira % booking oo bilaabanaya % sidaas darteed xiliga furitaanka uma bedeli kartid %',
            get_day_name(NEW.day_of_week), v_booking_count, v_min_start_time, NEW.opening_time
            USING ERRCODE = '45000';
END IF;

    IF
NEW.closing_time < v_max_end_time THEN
        RAISE EXCEPTION 'Malinka % da waxa jira % booking oo dhamanaya % sidaas darteed xiliga xidhitaanka uma bedeli kartid %',
            get_day_name(NEW.day_of_week), v_booking_count, v_max_end_time, NEW.closing_time
            USING ERRCODE = '45000';
END IF;

RETURN NEW;
END;
$$
LANGUAGE plpgsql;


CREATE TRIGGER trg_prevent_changing_time_and_close_stadium_when_events_exists
    BEFORE UPDATE
        OF is_open, opening_time, closing_time
    ON stadium_working_days
    FOR EACH ROW
    EXECUTE FUNCTION fn_prevent_changing_time_and_close_stadium_when_events_exists();
