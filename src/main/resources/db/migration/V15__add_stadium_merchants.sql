CREATE TABLE merchant_provider (
                                   id smallserial PRIMARY KEY,
                                   provider_name varchar(20) NOT NULL UNIQUE,
                                   provider_service varchar(50) NOT NULL
);


INSERT INTO merchant_provider(provider_name, provider_service)
VALUES
    ('Telesom', 'Zaad Service'),
    ('Somtel', 'eDahab'),
    ('Soltelco', 'eCash');


CREATE TABLE stadium_merchants (
                                   id smallserial PRIMARY KEY,
                                   merchant_number varchar(20) NOT NULL,
                                   stadium_id uuid NOT NULL REFERENCES stadiums(id) ON DELETE CASCADE,
                                   provider_id smallint NOT NULL REFERENCES merchant_provider(id) ON DELETE RESTRICT,

                                   CONSTRAINT stadium_merchant_unq
                                       UNIQUE (stadium_id, merchant_number, provider_id)
);