DROP FUNCTION generate_booking_time_seq_fn;

CREATE OR REPLACE FUNCTION generate_booking_time_seq_fn(
    p_field_id smallint,
    p_date date
)
    RETURNS jsonb
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_open          time;
    v_close         time;
    v_extra_time    smallint;
    v_slot_duration interval;
    v_slots         jsonb;
    v_day_of_week   smallint;
BEGIN

    v_day_of_week := EXTRACT(ISODOW FROM p_date);


    /*
       Get stadium working hours and extra time
    */
    SELECT swd.opening_time,
           swd.closing_time,
           s.extra_time

    INTO
        v_open,
        v_close,
        v_extra_time

    FROM fields f

             JOIN stadiums s
                  ON s.id = f.stadium_id

             JOIN stadium_working_days swd
                  ON swd.stadium_id = f.stadium_id
                      AND swd.day_of_week = v_day_of_week

    WHERE f.id = p_field_id
      AND swd.is_open = true;


    IF v_open IS NULL OR v_close IS NULL THEN
        RAISE EXCEPTION
            'Fadlan maalinta % garoonku ma shaqeynayo',
            get_day_name(v_day_of_week)
            USING ERRCODE = '45000';

    END IF;


    v_slot_duration :=
            interval '1 hour'
                + COALESCE(v_extra_time, 0) * interval '1 minute';


    /*
       Generate available booking slots
    */
    SELECT COALESCE(
                   jsonb_agg(
                           jsonb_build_object(
                                   'startTime', to_char(slot.start, 'YYYY-MM-DD HH24:MI:SS'),
                                   'endTime', to_char(slot.start + v_slot_duration, 'YYYY-MM-DD HH24:MI:SS'),
                                   'eventId', e.id,
                                   'eventKey', e.event_key,
                                   'eventStatus', CASE
                                                       WHEN e.id IS NULL
                                                           THEN 'available'
                                                       WHEN e.event_status = 'canceled'
                                                           THEN 'available'
                                                       ELSE e.event_status
                                       END)
                           ORDER BY slot.start), '[]'::jsonb)

    INTO v_slots
    FROM generate_series(
                 (p_date + v_open)::timestamp,
                 (p_date + v_close - v_slot_duration)::timestamp,
                 v_slot_duration
         ) AS slot(start)


             LEFT JOIN LATERAL (

        SELECT eb.id,
               eb.event_key,
               eb.event_status

        FROM event_bookings eb

        WHERE eb.field_id = p_field_id

          AND eb.event_start < slot.start + v_slot_duration

          AND eb.event_end > slot.start


        ORDER BY CASE
                     WHEN eb.event_status = 'canceled'
                         THEN 2
                     ELSE 1
                     END


        LIMIT 1


        ) e ON true;


    RETURN v_slots;

END;
$$;

