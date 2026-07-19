CREATE TABLE field_images
(
    id         smallserial primary key,
    image_path text,
    field_id   smallint references fields (id) on delete cascade
);