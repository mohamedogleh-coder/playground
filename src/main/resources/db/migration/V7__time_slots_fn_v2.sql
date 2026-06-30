CREATE OR REPLACE FUNCTION generate_booking_time_seq_fn(
    p_field_id smallint,
    p_date date
) RETURNS jsonb
    LANGUAGE plpgsql AS
$$
DECLARE
    v_open          time;
    v_close         time;
    v_extra_time    smallint;
    v_slot_duration interval;
    v_slots         jsonb;
BEGIN
    SELECT d.opening_time, d.closing_time, s.extra_time
    INTO v_open, v_close, v_extra_time
    FROM fields f
             JOIN stadium_working_days d ON f.stadium_id = d.stadium_id
             JOIN stadiums s ON s.id = f.stadium_id
    WHERE f.id = p_field_id
      AND d.day_of_week = EXTRACT(ISODOW FROM p_date);

    IF v_open IS NULL THEN
        RETURN jsonb_build_object('timeSlots', '[]'::jsonb);
    END IF;

    v_slot_duration := interval '1 hour' + (COALESCE(v_extra_time, 0::smallint) * interval '1 minute');

    SELECT jsonb_agg(
                   jsonb_build_object(
                           'start_time',   to_char(slot.start, 'YYYY-MM-DD HH24:MI:SS'),
                           'end_time',     to_char(slot.start + v_slot_duration, 'YYYY-MM-DD HH24:MI:SS'),
                           'is_available', (e.id IS NULL),
                           'event_id',     e.id
                   )
           )
    INTO v_slots
    FROM generate_series(
                 (p_date + v_open)::timestamp,
                 (p_date + v_close - v_slot_duration)::timestamp,
                 v_slot_duration
         ) AS slot(start)
             LEFT JOIN LATERAL (
        SELECT id
        FROM event_bookings
        WHERE field_id = p_field_id
          AND event_start < (slot.start + v_slot_duration)
          AND event_end > slot.start
        LIMIT 1
        ) e ON true;

     RETURN jsonb_build_object(
            'timeSlots', COALESCE(v_slots, '[]'::jsonb)
           );
END;
$$;