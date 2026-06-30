ALTER TABLE events RENAME TO event_bookings;

CREATE OR REPLACE FUNCTION generate_booking_time_seq_fn(
    p_field_id smallint,
    p_date date
)
    RETURNS jsonb
AS
$$
DECLARE
v_opening_time time;
    v_closing_time time;
    v_day_of_week  smallint;
    v_time_slots   jsonb;
BEGIN
    -- 1. Si toos ah uga soo saar lambarka maalinta (1 = Monday, 7 = Sunday)
    v_day_of_week := EXTRACT(ISODOW FROM p_date);

    -- 2. Soo qaado saacadaha furitaanka iyo xiritaanka
SELECT d.opening_time, d.closing_time
INTO v_opening_time, v_closing_time
FROM fields f
         JOIN stadium_working_days d ON f.stadium_id = d.stadium_id
WHERE f.id = p_field_id
  AND d.day_of_week = v_day_of_week;

-- Haddii garoonku xiran yahay maalintaas
IF v_opening_time IS NULL OR v_closing_time IS NULL THEN
        RETURN jsonb_build_object(
                'field_id', p_field_id,
                'is_open', false,
                'date', p_date,
                'timeSlots', jsonb_build_array()
               );
END IF;

    -- 3. Dhal saacadaha (Slots grid) isla markaana la xiriiri (Join) shaxda events
SELECT jsonb_agg(
               jsonb_build_object(
                       'start_time', to_char(sg.slot_start, 'YYYY-MM-DD HH24:MI:SS'),
                       'end_time', to_char(sg.slot_end, 'YYYY-MM-DD HH24:MI:SS'),
                   -- Haddii e.id uu NULL yahay, waxay la dhigan tahay boosku waa banaan yahay (true)
                       'is_available', CASE WHEN e.id IS NULL THEN true ELSE false END,
                   -- Halkan waxaynu ku soo celinaynaa xogta event-ka haddii uu overlap jiro
                       'event_id', e.id,
                       'event_key', e.event_key
               )
       )
INTO v_time_slots
FROM (
         -- Dhalinta shabaqa saacadaha (Base Grid)
         SELECT series_time                      AS slot_start,
                series_time + '1 hour'::interval AS slot_end
         FROM generate_series(
                      (p_date + v_opening_time)::timestamp,
                      (p_date + v_closing_time - '1 hour'::interval)::timestamp,
                      '1 hour'::interval
              ) AS series_time) sg
         -- Kani wuxuu saacad kasta u raadinayaa event-ka ku aaddan haddii uu jiro
         LEFT JOIN LATERAL (
    SELECT id, event_key
    FROM event_bookings
    WHERE field_id = p_field_id
      AND event_start < sg.slot_end
      AND event_end > sg.slot_start
        LIMIT 1 -- Sugitaan ahaan, si uusan hal slot labo saf u dhalin
        ) e ON true;

RETURN jsonb_build_object(
        'field_id', p_field_id,
        'is_open', true,
        'date', p_date,
        'time_slots', COALESCE(v_time_slots, jsonb_build_array())
       );
END;
$$ LANGUAGE plpgsql;
