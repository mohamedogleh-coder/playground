ALTER TABLE event_bookings
    ADD COLUMN event_status varchar(20)
        DEFAULT 'full'
        CHECK (event_status IN ('half', 'full'))
        NOT NULL;

ALTER TABLE event_bookings
    ADD COLUMN remaining numeric(12, 2) default 0
        NOT NULL;

ALTER TABLE event_bookings
    ADD CONSTRAINT event_bookings_remaining_check
        CHECK (
            (event_status = 'full' AND remaining = 0)
                OR
            (event_status = 'half' AND remaining > 0)
            );

CREATE TABLE event_booking_payments
(
    id              serial PRIMARY KEY,
    event_id        int            NOT NULL REFERENCES event_bookings (id) ON DELETE CASCADE,
    payer_id        uuid           NOT NULL,
    received_by_id  uuid,
    payment_method  varchar(20)    NOT NULL,
    merchant_number varchar(20),
    amount_paid     numeric(12, 2) NOT NULL,
    discount_amount numeric(12, 2)          DEFAULT 0,
    paid_at         timestamp      NOT NULL DEFAULT now()
);






