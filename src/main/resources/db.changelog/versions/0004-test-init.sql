--changeset TUTSK:1
CREATE TABLE IF NOT EXISTS DADD (
                                    id BIGSERIAL PRIMARY KEY,
                                    name VARCHAR(50) NOT NULL,
    sum BIGINT,
    car_id BIGINT,
    CONSTRAINT fk_car FOREIGN KEY (car_id) REFERENCES car(id)
    );
