CREATE
OR REPLACE FUNCTION generate_time_slots_fn(
    p_date       date,
    p_open_time  time,
    p_close_time time,
    p_extra_time smallint,
    p_time_gap   interval
)
    RETURNS TABLE
            (
                start_time timestamp,
                end_time   timestamp
            )
AS
$$
DECLARE
v_step interval;
BEGIN

    IF
p_open_time IS NULL
        OR p_close_time IS NULL THEN
        RAISE EXCEPTION 'Opening time and closing time are required';
END IF;

    IF
p_open_time >= p_close_time THEN
        RAISE EXCEPTION 'Opening time (%) must be before closing time (%)',
            p_open_time,
            p_close_time;
END IF;

    v_step
:= p_time_gap + (COALESCE(p_extra_time, 0) || ' minutes')::interval;

RETURN QUERY
SELECT slot              AS start_time,
       slot + p_time_gap AS end_time
FROM generate_series(
             p_date + p_open_time,
             p_date + p_close_time - p_time_gap,
             v_step
     ) slot;

END;
$$
LANGUAGE plpgsql;


---------------------------------------------- get_field_events_fn ------------------------------------------------------

CREATE
OR REPLACE FUNCTION get_field_events_fn(
    p_date     date,
    p_field_id smallint
)
    RETURNS TABLE
            (
                field_id smallint,
                capacity smallint,
                cost     numeric(12, 2),
                slots    jsonb
            )
AS
$$
DECLARE
v_open_time  time;
    v_close_time
time;
    v_extra_time
smallint;
    v_is_open
boolean;
    v_capacity
smallint;
    v_cost
numeric(12, 2);

BEGIN

SELECT f.capacity,
       f.cost,
       swd.opening_time,
       swd.closing_time,
       swd.is_open,
       s.extra_time
INTO
    v_capacity,
    v_cost,
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

IF
NOT FOUND OR v_is_open IS NOT TRUE THEN
        RETURN QUERY
SELECT p_field_id,
       v_capacity,
       v_cost,
       '[]'::jsonb;
RETURN;
END IF;

RETURN QUERY
SELECT p_field_id,
       v_capacity,
       v_cost,
       jsonb_agg(
               jsonb_build_object(
                       'startTime', slots.start_time,
                       'endTime', slots.end_time,
                       'isAvailable', CASE
                                          WHEN eb.id IS NOT NULL THEN false
                                          ELSE true
                           END,
                       'event_id', eb.id,
                       'eventKey', eb.event_key
               ) ORDER BY slots.start_time
       )

FROM generate_time_slots_fn(
             p_date,
             v_open_time,
             v_close_time,
             v_extra_time,
             interval '1 hour'
     ) slots

         LEFT JOIN event_bookings eb
                   ON eb.field_id = p_field_id
                       AND eb.event_start < slots.end_time
                       AND eb.event_end > slots.start_time;

END;
$$
LANGUAGE plpgsql;

