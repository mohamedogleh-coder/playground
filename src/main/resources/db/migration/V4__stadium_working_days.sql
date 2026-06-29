CREATE TABLE stadium_working_days
(
    id            BIGSERIAL PRIMARY KEY,
    stadium_id    UUID NOT NULL REFERENCES stadiums(id) ON DELETE CASCADE,
    day_of_week   SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    opening_time  TIME NOT NULL,
    closing_time  TIME NOT NULL,
    is_open       BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT valid_working_hours
        CHECK (opening_time < closing_time),

    CONSTRAINT stadium_day_unique
        UNIQUE (stadium_id, day_of_week)
);



ALTER TABLE stadiums
    ADD COLUMN extra_time smallint default 5 not null;
ALTER TABLE stadiums
    ADD COLUMN profile_url text;


