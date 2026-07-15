ALTER TABLE stadiums
    ADD COLUMN half_booking boolean DEFAULT FALSE;

DROP VIEW IF EXISTS get_stadiums_view;


CREATE VIEW get_stadiums_view AS
SELECT s.id,
       s.stadium_name,
       ST_X(s.location::geometry) AS longitude,
       ST_Y(s.location::geometry) AS latitude,
       profile_url,
       extra_time,
       half_booking,
       COUNT(f.id)                AS num_of_fields
FROM stadiums s
         JOIN public.fields f ON s.id = f.stadium_id
GROUP BY s.id, s.stadium_name, s.location
ORDER BY num_of_fields DESC;
