CREATE OR REPLACE FUNCTION get_day_name(p_day_number INT)
    RETURNS VARCHAR
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_day_name VARCHAR;
BEGIN
    CASE p_day_number
        WHEN 1 THEN v_day_name := 'Isniin';
        WHEN 2 THEN v_day_name := 'Salaasa';
        WHEN 3 THEN v_day_name := 'Arbaca';
        WHEN 4 THEN v_day_name := 'Khamiis';
        WHEN 5 THEN v_day_name := 'Jimce';
        WHEN 6 THEN v_day_name := 'Sabti';
        WHEN 7 THEN v_day_name := 'Axad';
        ELSE RAISE EXCEPTION 'Invalid day number %. Day number must be between 1 and 7', p_day_number;
        END CASE;

    RETURN v_day_name;
END;
$$;


CREATE
    OR REPLACE FUNCTION fn_check_working_day_conflicts()
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

    SELECT count(*)::int,
           min(eb.event_start::time),
           max(eb.event_end::time)
    INTO
        v_booking_count,
        v_min_start_time,
        v_max_end_time
    FROM event_bookings eb
             JOIN fields f ON f.id = eb.field_id
    WHERE f.stadium_id = NEW.stadium_id
      AND extract(isodow FROM eb.event_start) = NEW.day_of_week
      AND eb.event_start >= now();

    IF
        v_booking_count = 0 THEN
        RETURN NEW;
    END IF;

    IF NEW.is_open = false THEN
        RAISE EXCEPTION 'Malinka % da waxa jira % booking sidaas darteed lama ogola in la xidho garoon-ka',
            get_day_name(NEW.day_of_week), v_booking_count
            USING ERRCODE = 'P0001';
    END IF;

    IF NEW.opening_time > v_min_start_time THEN
        RAISE EXCEPTION 'Malinka % da waxa jira % booking oo bilaabanaya % sidaas darteed xiliga furitaanka uma bedeli kartid %',
            get_day_name(NEW.day_of_week), v_booking_count, v_min_start_time, NEW.opening_time
            USING ERRCODE = 'P0001';
    END IF;

    IF NEW.closing_time < v_max_end_time THEN
        RAISE EXCEPTION 'Malinka % da waxa jira % booking oo dhamanaya % sidaas darteed xiliga xidhitaanka uma bedeli kartid %',
            get_day_name(NEW.day_of_week), v_booking_count, v_max_end_time, NEW.closing_time
            USING ERRCODE = 'P0001';
    END IF;

    RETURN NEW;
END;
$$
    LANGUAGE plpgsql;


CREATE TRIGGER trg_check_working_day_conflicts
    BEFORE UPDATE
    ON stadium_working_days
    FOR EACH ROW
EXECUTE FUNCTION fn_check_working_day_conflicts();
