CREATE TABLE stadiums
(
    id           uuid primary key,
    stadium_name varchar(100) not null unique,
    location     GEOGRAPHY(Point, 4326)
);