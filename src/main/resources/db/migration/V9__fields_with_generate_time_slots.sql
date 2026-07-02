CREATE OR REPLACE FUNCTION generate_time_slots(p_field_id smallint, p_date date)
    RETURNS jsonb as
$$
DECLARE
    v_open          time;
    v_close         time;
    v_extra_time    smallint;
    v_field_id      smallint;
    v_capacity      smallint;
    v_cost          numeric(12, 2);
    v_slots         jsonb;
    v_slot_duration interval;

BEGIN
    SELECT f.id, f.capacity, f.cost, d.opening_time, d.closing_time, s.extra_time
    INTO v_field_id,v_capacity,v_cost,v_open, v_close, v_extra_time
    FROM fields f
            LEFT JOIN stadium_working_days d ON f.stadium_id = d.stadium_id AND d.day_of_week = EXTRACT(ISODOW FROM p_date)
             JOIN stadiums s ON s.id = f.stadium_id
    WHERE f.id = p_field_id;


    IF v_open IS NULL THEN
        RETURN jsonb_build_object('fieldId', v_field_id, 'capacity', v_capacity, 'cost', v_cost, 'slots',
                                  '[]'::jsonb);
    END IF;

    v_slot_duration := interval '1 hour' + (COALESCE(v_extra_time, 0::smallint) * interval '1 minute');

    SELECT jsonb_agg(
                   jsonb_build_object(
                           'startTime',   to_char(slot.start, 'YYYY-MM-DD HH24:MI:SS'),
                           'endTime',     to_char(slot.start + v_slot_duration, 'YYYY-MM-DD HH24:MI:SS'),
                           'isAvailable', (e.id IS NULL),
                           'eventId',     e.id
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

    RETURN jsonb_build_object('fieldId', v_field_id, 'capacity', v_capacity, 'cost', v_cost, 'slots',
                              COALESCE(v_slots, '[]'::jsonb)::jsonb);

end;
$$ LANGUAGE plpgsql;
