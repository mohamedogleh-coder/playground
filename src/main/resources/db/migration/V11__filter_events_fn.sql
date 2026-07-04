CREATE OR REPLACE FUNCTION filter_events_fn(
    p_time timestamp,
    p_latitude DOUBLE PRECISION,
    p_longitude DOUBLE PRECISION,
    p_capacity SMALLINT,
    p_radius INTEGER DEFAULT 10000
)
    RETURNS TABLE
            (
                stadium_id UUID,
                stadium_name varchar,
                distance DOUBLE PRECISION,
                field_id smallint,
                capacity SMALLINT,
                field_cost numeric(12,2),
                longitude DOUBLE PRECISION,
                latitude DOUBLE PRECISION
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
RETURN QUERY
SELECT DISTINCT ON (s.id)
    s.id AS stadium_id,
    s.stadium_name,
    CASE
    WHEN p_latitude IS NULL OR p_longitude IS NULL THEN NULL
    ELSE ST_Distance(
    s.location::geography,
    ST_SetSRID(ST_MakePoint(p_longitude, p_latitude),4326)::geography
    )
END AS distance,
            f.id AS field_id,
            f.capacity,
            f.cost AS field_cost,
            ST_X(s.location::geometry) AS longitude,
            ST_Y(s.location::geometry) AS latitude
        FROM stadiums s
                 JOIN fields f ON s.id = f.stadium_id
                 JOIN stadium_working_days swd ON s.id = swd.stadium_id
        WHERE swd.day_of_week = EXTRACT(ISODOW FROM p_time)
          AND swd.is_open = true
          AND f.capacity = p_capacity
          AND (
            p_latitude IS NULL
                OR p_longitude IS NULL
                OR ST_DWithin(
                    s.location::geography,
                    ST_SetSRID(ST_MakePoint(p_longitude, p_latitude),4326)::geography,
                    p_radius
                   )
            )
          AND NOT EXISTS (
            SELECT 1
            FROM event_bookings eb
            WHERE eb.field_id = f.id
              AND eb.event_start = p_time
        )
        ORDER BY
            s.id,
            distance NULLS LAST,
            f.id;
END;
$$;