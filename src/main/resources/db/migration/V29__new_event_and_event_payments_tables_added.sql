DROP TABLE IF EXISTS event_payments;
DROP TABLE IF EXISTS event_bookings;

DROP TRIGGER IF EXISTS trg_check_before_booking_event ON event_bookings;
DROP FUNCTION IF EXISTS check_before_booking_event_fn;

CREATE TABLE event_bookings
(
    id             serial PRIMARY KEY,
    field_id       smallint       NOT NULL REFERENCES fields (id) ON DELETE RESTRICT,
    event_start    timestamp      NOT NULL,
    event_end      timestamp      NOT NULL,
    extra_time     smallint       NOT NULL DEFAULT 0,
    event_key      varchar(4),
    payment_status varchar(10)    NOT NULL DEFAULT 'paid' CHECK (payment_status IN ('paid', 'partial', 'refunded')),

    event_status   varchar(10)    NOT NULL
                                           DEFAULT 'confirmed' CHECK (event_status IN ('pending', 'confirmed', 'completed', 'canceled')),

    remaining      numeric(12, 2) NOT NULL DEFAULT 0.00,
    description    varchar(50),
    CONSTRAINT events_remaining_check
        CHECK (
            (payment_status = 'partial' AND remaining > 0)
                OR
            (payment_status = 'paid' AND remaining = 0)
                OR
            (payment_status = 'refunded')
            )
);

CREATE TABLE event_payments
(
    id              serial primary key,
    event_id        int            not null references event_bookings (id) on delete restrict,
    paid_user       uuid,
    received_by     uuid,
    payment_method  varchar(20),
    merchant_number varchar(20),
    amount_paid     numeric(12, 2) not null CHECK (amount_paid > 0),
    discounted      numeric(12, 2) NOT NULL DEFAULT 0,
    paid_at         timestamptz    NOT NULL DEFAULT now(),
    CONSTRAINT payment_person_check
        CHECK (
            paid_user IS NOT NULL
                OR
            received_by IS NOT NULL
            )
);

CREATE
    OR REPLACE FUNCTION check_before_booking_event_fn()
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
    v_day_of_week
        := EXTRACT(ISODOW FROM NEW.event_start);

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

    IF
        NOT FOUND OR v_is_open IS NOT TRUE OR v_open_time IS NULL THEN
        RAISE EXCEPTION 'Fadlan malinka % ta ee aad doratay garoonku ma shaqaynayo',get_day_name(v_day_of_week) USING ERRCODE = '45000';
    END IF;

    IF
        v_stop_booking IS TRUE THEN
        RAISE EXCEPTION 'Fadlan playground-kan adminka ayaa booking ka joojey' USING ERRCODE = '45000';
    END IF;

    IF
        v_half_booking IS NOT TRUE AND NEW.payment_status = 'partial' THEN
        RAISE EXCEPTION 'Fadlan policy-ga garoon-ka ka mid maha in qayb booking-ka laga qaato' USING ERRCODE = '45000';
    END IF;

    IF
        NEW.event_start::TIME < v_open_time OR NEW.event_start::TIME > v_close_time THEN
        RAISE EXCEPTION 'Fadlan % garoonku ma shaqeeyo', NEW.event_start::time USING ERRCODE = '45000';
    END IF;

    v_slot_duration_minutes := 60 + v_extra_time;
    v_minutes_since_open
        := EXTRACT(EPOCH FROM (NEW.event_start::TIME - v_open_time)) / 60;

    IF
        MOD(v_minutes_since_open, v_slot_duration_minutes::NUMERIC) <> 0 THEN
        RAISE EXCEPTION 'Fadlan % ka mid maha xiliyada garoonkan laga heli karo', NEW.event_start::time USING ERRCODE = '45000';
    END IF;

    NEW.extra_time
        := v_extra_time;
    NEW.event_end
        := NEW.event_start + make_interval(mins => v_slot_duration_minutes);

    IF
        EXISTS (SELECT 1
                FROM event_bookings e
                WHERE e.field_id = NEW.field_id
                  AND e.id IS DISTINCT FROM NEW.id
                  AND e.event_start < NEW.event_end
                  AND e.event_end > NEW.event_start) THEN
        RAISE EXCEPTION 'Fadlan maalinka % ta saacada % waa lagaa hor qabsaday', get_day_name(v_day_of_week),NEW.event_start::time USING ERRCODE = '45000';
    END IF;

    RETURN NEW;
END;
$$
    LANGUAGE plpgsql;


CREATE TRIGGER trg_check_before_booking_event
    BEFORE INSERT OR
        UPDATE OF event_start, field_id, event_status
    ON event_bookings
    FOR EACH ROW
    WHEN (
        pg_trigger_depth() = 0
        )
EXECUTE FUNCTION check_before_booking_event_fn();



