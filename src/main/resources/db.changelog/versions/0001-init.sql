--changeset TUTSK:1
CREATE TABLE IF NOT EXISTS car (
                                   id BIGINT PRIMARY KEY,
                                   name VARCHAR(50) NOT NULL,
    price BIGINT NOT NULL
    );

--changeset TUTSK:2
CREATE TABLE IF NOT EXISTS loan (
                                    id BIGSERIAL PRIMARY KEY,
                                    name VARCHAR(50) NOT NULL,
    sum BIGINT,
    car_id BIGINT,
    CONSTRAINT fk_car FOREIGN KEY (car_id) REFERENCES car(id)
    );
