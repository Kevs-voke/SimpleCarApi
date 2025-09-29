CREATE TABLE car (
    id BIGSERIAL PRIMARY KEY,
    vin VARCHAR(17) NOT NULL UNIQUE,
    make VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    manufacture_year INT NOT NULL,
    CHECK (manufacture_year BETWEEN 1886 AND EXTRACT(YEAR FROM CURRENT_DATE)),
    color VARCHAR(50),
    engine_type VARCHAR(50),
    registration_number VARCHAR(20) UNIQUE
);

CREATE INDEX idx_car_vin ON car (vin);
