CREATE
EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE events
(
    id          serial primary key,
    field_id    smallint  not null references fields(id) on  delete restrict ,
    event_start timestamp not null,
    event_end   timestamp not null,
    event_key   int,
        CONSTRAINT no_overlapping_events EXCLUDE USING gist (
        field_id WITH =,
        tsrange(event_start, event_end) WITH &&
    )
);