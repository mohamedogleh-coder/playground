CREATE TABLE event_merchant_payments
(
    id              serial primary key,
    payment_id      int            not null references event_payments (id) on delete cascade,
    merchant_number varchar(20)    not null,
    payment_method  varchar(20)    not null,
    amount_paid     numeric(12, 2) not null
        check (amount_paid > 0)
);

ALTER TABLE event_payments
DROP COLUMN IF EXISTS payment_method,
    DROP COLUMN IF EXISTS merchant_number,
    DROP COLUMN IF EXISTS amount_paid;

