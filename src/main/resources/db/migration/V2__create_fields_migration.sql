CREATE TABLE fields
(
    id         smallserial primary key,
    cost       numeric(12, 2) not null check ( cost > 0 ),
    capacity   smallint       not null check ( capacity > 1 ),
    stadium_id uuid           not null references stadiums (id) on delete cascade
);




