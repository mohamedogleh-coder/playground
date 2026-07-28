CREATE TABLE expenses
(
    id            serial primary key,
    expense_title varchar(50) not null,
    stadium_id    uuid references stadiums (id) on delete cascade,
    description   varchar(100),
    total_amount  NUMERIC(12, 2),
    expense_date  TIMESTAMP DEFAULT now()
);