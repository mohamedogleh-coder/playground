ALTER TABLE event_booking_payments
    ALTER COLUMN payer_id DROP NOT NULL;


ALTER TABLE event_booking_payments
    ADD CONSTRAINT payment_payer_or_receiver_check
        CHECK (
            payer_id IS NOT NULL
                OR
            received_by_id IS NOT NULL
            );