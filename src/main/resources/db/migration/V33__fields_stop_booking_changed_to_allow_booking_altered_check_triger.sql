ALTER TABLE fields
    RENAME COLUMN stop_booking TO allow_booking;


DROP FUNCTION IF EXISTS get_day_name();


CREATE
OR REPLACE FUNCTION get_day_name(p_day_number INT)
    RETURNS VARCHAR
    LANGUAGE plpgsql
AS
$$
DECLARE
v_day_name VARCHAR;
BEGIN
CASE p_day_number
        WHEN 1 THEN v_day_name := 'Isniin-ta';
WHEN 2 THEN v_day_name := 'Salaasa-da';
WHEN 3 THEN v_day_name := 'Arbaca-da';
WHEN 4 THEN v_day_name := 'Khamiis-ta';
WHEN 5 THEN v_day_name := 'Jimce-da';
WHEN 6 THEN v_day_name := 'Sabti-da';
WHEN 7 THEN v_day_name := 'Axad-da';
ELSE
            RAISE EXCEPTION
            'Invalid day number %. Day number must be between 1 and 7',
            p_day_number;
END
CASE;

RETURN v_day_name;
END;
$$;


DROP TRIGGER IF EXISTS trg_check_before_booking_event
ON event_bookings;


DROP FUNCTION IF EXISTS check_before_booking_event_fn();


CREATE
OR REPLACE FUNCTION public.check_before_booking_event_fn()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$function$

DECLARE
v_extra_time              SMALLINT;
    v_day_of_week
INT;
    v_open_time
TIME;
    v_close_time
TIME;
    v_half_booking
BOOLEAN;
    v_allow_booking
BOOLEAN;
    v_is_open
BOOLEAN;
    v_slot_duration_minutes
INT;
    v_minutes_since_open
NUMERIC;

BEGIN

    v_day_of_week
:= EXTRACT(ISODOW FROM NEW.event_start);


SELECT s.extra_time,
       swd.opening_time,
       swd.closing_time,
       s.half_booking,
       swd.is_open,
       f.allow_booking

INTO
    v_extra_time,
    v_open_time,
    v_close_time,
    v_half_booking,
    v_is_open,
    v_allow_booking

FROM fields f

         JOIN stadiums s
              ON s.id = f.stadium_id

         JOIN stadium_working_days swd
              ON swd.stadium_id = f.stadium_id
                  AND swd.day_of_week = v_day_of_week

WHERE f.id = NEW.field_id;


IF
v_half_booking IS NOT TRUE
       AND NEW.payment_status = 'partial' THEN

        RAISE EXCEPTION
        'Fadlan policy-ga garoonkan ma ogola half booking'
        USING ERRCODE = '45000';

END IF;

IF
NOT FOUND
       OR v_is_open IS NOT TRUE
       OR v_open_time IS NULL THEN

        RAISE EXCEPTION
        'Fadlan maalinka % garoonku ma shaqaynayo',
        get_day_name(v_day_of_week)
        USING ERRCODE = '45000';

END IF;

    -- allow_booking = false means booking disabled
    IF
v_allow_booking IS FALSE THEN
        RAISE EXCEPTION 'Field-kan booking ka waa la joojiyay' USING ERRCODE = '45000';
END IF;

    IF
NEW.event_start::TIME < v_open_time
       OR NEW.event_start::TIME > v_close_time THEN

        RAISE EXCEPTION
        'Fadlan maalinka % saacada % garoonku ma shaqeeyo',
        get_day_name(v_day_of_week),
        NEW.event_start::TIME
        USING ERRCODE = '45000';

END IF;

    v_slot_duration_minutes
:= 60 + v_extra_time;


    v_minutes_since_open
:=
        EXTRACT(
            EPOCH FROM
            (NEW.event_start::TIME - v_open_time)
        ) / 60;



    IF
MOD(
        v_minutes_since_open,
        v_slot_duration_minutes::NUMERIC
    ) <> 0 THEN

        RAISE EXCEPTION
        '% kama mid aha xiliyada la heli karo malinka %',NEW.event_start::TIME, get_day_name(v_day_of_week) USING ERRCODE = '45000';

END IF;

    NEW.extra_time
:=
        v_extra_time;


    NEW.event_end
:=
        NEW.event_start
        + make_interval(
            mins => v_slot_duration_minutes
        );



    IF
EXISTS (

        SELECT 1
        FROM event_bookings e

        WHERE e.field_id = NEW.field_id

          AND e.id IS DISTINCT FROM NEW.id

          AND e.event_start < NEW.event_end

          AND e.event_end > NEW.event_start

    ) THEN

        RAISE EXCEPTION
        'Maalinka % saacada % waa la qabsaday',
        get_day_name(v_day_of_week),
        NEW.event_start::TIME
        USING ERRCODE = '45000';

END IF;



RETURN NEW;

END;

$function$;



CREATE TRIGGER trg_check_before_booking_event

    BEFORE INSERT OR
UPDATE OF event_start, field_id, event_status

ON event_bookings
    FOR EACH ROW
    WHEN (
    pg_trigger_depth() = 0
    )
    EXECUTE FUNCTION check_before_booking_event_fn();