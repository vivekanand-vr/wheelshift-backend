-- V3__Fix_Column_Types.sql
-- Fix data type mismatches between schema and JPA entities

-- Change gears from TINYINT to INT in car_models table to match JPA Integer type
ALTER TABLE car_models
    MODIFY COLUMN gears INT;

-- Change doors and seats from TINYINT to INT in car_detailed_specs table to match JPA Integer type
ALTER TABLE car_detailed_specs
    MODIFY COLUMN doors INT,
    MODIFY COLUMN seats INT;

-- Change year from SMALLINT to INT in cars table to match JPA Integer type
ALTER TABLE cars
    MODIFY COLUMN year INT NOT NULL;
