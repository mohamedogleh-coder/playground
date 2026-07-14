DROP FUNCTION get_field_events_fn(p_date date, p_field_id smallint);


CREATE OR REPLACE FUNCTION get_field_events_fn(
    p_date date,
    p_field_id smallint
)
    RETURNS jsonb
    LANGUAGE plpgsql
AS
$$
DECLARE
v_open_time  time;
    v_close_time time;
    v_extra_time smallint;
    v_is_open    boolean;
    v_slots      jsonb;
BEGIN

SELECT swd.opening_time,
       swd.closing_time,
       swd.is_open,
       s.extra_time
INTO
    v_open_time,
    v_close_time,
    v_is_open,
    v_extra_time
FROM fields f
         JOIN stadiums s
              ON s.id = f.stadium_id
         JOIN stadium_working_days swd
              ON swd.stadium_id = s.id
                  AND swd.day_of_week = EXTRACT(ISODOW FROM p_date)
WHERE f.id = p_field_id;

IF NOT FOUND OR NOT v_is_open THEN
        RETURN '[]'::jsonb;
END IF;

SELECT jsonb_agg(
               jsonb_build_object(
                       'startTime', slots.start_time,
                       'endTime', slots.end_time,
                       'eventId', eb.id,
                       'eventKey', eb.event_key,
                       'eventStatus', coalesce(eb.event_status,'available')
               )
                   ORDER BY slots.start_time
       )
INTO v_slots
FROM generate_time_slots_fn(
             p_date,
             v_open_time,
             v_close_time,
             v_extra_time,
             interval '1 hour'
     ) AS slots
         LEFT JOIN event_bookings eb
                   ON eb.field_id = p_field_id
                       AND eb.event_start < slots.end_time
                       AND eb.event_end > slots.start_time;

RETURN COALESCE(v_slots, '[]'::jsonb);
END;
$$;
