CREATE TABLE stadium_managers
(
    id smallserial primary key ,
    manager_id  uuid not null,
    stadium_id  uuid not null references stadiums (id) on delete cascade,
    date_joined date default current_date,
    constraint stadium_manager_unq UNIQUE (manager_id, stadium_id)
);