-- V10__Seed_Motorcycle_Data.sql
-- Seed data for motorcycle models and sample motorcycles

-- =====================================================
-- Seed: Motorcycle Models
-- =====================================================

-- Honda Models
INSERT INTO motorcycle_models (make, model, variant, year, engine_capacity, fuel_type, transmission_type, vehicle_type, seating_capacity, is_active) VALUES
('Honda', 'Activa 6G', 'Standard', 2024, 109, 'PETROL', 'CVT', 'SCOOTER', 2, TRUE),
('Honda', 'Activa 6G', 'Deluxe', 2024, 109, 'PETROL', 'CVT', 'SCOOTER', 2, TRUE),
('Honda', 'CB Shine', 'Standard', 2024, 124, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Honda', 'CB Hornet 2.0', 'Standard', 2024, 184, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Honda', 'SP 125', 'Drum', 2024, 124, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Honda', 'SP 125', 'Disc', 2024, 124, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Honda', 'Dio', 'Standard', 2024, 109, 'PETROL', 'CVT', 'SCOOTER', 2, TRUE),
('Honda', 'Unicorn', 'Standard', 2024, 162, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE);

-- Hero Models
INSERT INTO motorcycle_models (make, model, variant, year, engine_capacity, fuel_type, transmission_type, vehicle_type, seating_capacity, is_active) VALUES
('Hero', 'Splendor Plus', 'Standard', 2024, 97, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Hero', 'Splendor Plus', 'XTEC', 2024, 97, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Hero', 'HF Deluxe', 'Standard', 2024, 97, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Hero', 'Passion Pro', 'Standard', 2024, 113, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Hero', 'Glamour', 'Standard', 2024, 125, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Hero', 'Xtreme 160R', 'Standard', 2024, 163, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Hero', 'Maestro Edge', 'Standard', 2024, 110, 'PETROL', 'CVT', 'SCOOTER', 2, TRUE),
('Hero', 'Pleasure Plus', 'Standard', 2024, 110, 'PETROL', 'CVT', 'SCOOTER', 2, TRUE);

-- Yamaha Models
INSERT INTO motorcycle_models (make, model, variant, year, engine_capacity, fuel_type, transmission_type, vehicle_type, seating_capacity, is_active) VALUES
('Yamaha', 'R15 V4', 'Standard', 2024, 155, 'PETROL', 'MANUAL', 'SPORT_BIKE', 2, TRUE),
('Yamaha', 'R15 V4', 'M', 2024, 155, 'PETROL', 'MANUAL', 'SPORT_BIKE', 2, TRUE),
('Yamaha', 'R15S', 'Standard', 2024, 155, 'PETROL', 'MANUAL', 'SPORT_BIKE', 2, TRUE),
('Yamaha', 'MT-15', 'Version 2.0', 2024, 155, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Yamaha', 'FZ-S FI', 'Version 4.0', 2024, 149, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Yamaha', 'FZ-X', 'Standard', 2024, 149, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Yamaha', 'Fascino 125', 'Standard', 2024, 125, 'PETROL', 'CVT', 'SCOOTER', 2, TRUE),
('Yamaha', 'Ray ZR', 'Street Rally', 2024, 125, 'PETROL', 'CVT', 'SCOOTER', 2, TRUE);

-- Royal Enfield Models
INSERT INTO motorcycle_models (make, model, variant, year, engine_capacity, fuel_type, transmission_type, vehicle_type, seating_capacity, is_active) VALUES
('Royal Enfield', 'Classic 350', 'Single Channel ABS', 2024, 349, 'PETROL', 'MANUAL', 'CRUISER', 2, TRUE),
('Royal Enfield', 'Classic 350', 'Dual Channel ABS', 2024, 349, 'PETROL', 'MANUAL', 'CRUISER', 2, TRUE),
('Royal Enfield', 'Meteor 350', 'Fireball', 2024, 349, 'PETROL', 'MANUAL', 'CRUISER', 2, TRUE),
('Royal Enfield', 'Meteor 350', 'Stellar', 2024, 349, 'PETROL', 'MANUAL', 'CRUISER', 2, TRUE),
('Royal Enfield', 'Himalayan', 'Standard', 2024, 411, 'PETROL', 'MANUAL', 'OFF_ROAD', 2, TRUE),
('Royal Enfield', 'Hunter 350', 'Metro', 2024, 349, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Royal Enfield', 'Hunter 350', 'Retro', 2024, 349, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Royal Enfield', 'Bullet 350', 'Standard', 2024, 346, 'PETROL', 'MANUAL', 'CRUISER', 2, TRUE);

-- TVS Models
INSERT INTO motorcycle_models (make, model, variant, year, engine_capacity, fuel_type, transmission_type, vehicle_type, seating_capacity, is_active) VALUES
('TVS', 'Apache RTR 160', '4V', 2024, 159, 'PETROL', 'MANUAL', 'SPORT_BIKE', 2, TRUE),
('TVS', 'Apache RTR 180', 'Standard', 2024, 177, 'PETROL', 'MANUAL', 'SPORT_BIKE', 2, TRUE),
('TVS', 'Apache RTR 200', '4V', 2024, 197, 'PETROL', 'MANUAL', 'SPORT_BIKE', 2, TRUE),
('TVS', 'Raider 125', 'Drum', 2024, 125, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('TVS', 'Raider 125', 'Disc', 2024, 125, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('TVS', 'Jupiter 125', 'Standard', 2024, 125, 'PETROL', 'CVT', 'SCOOTER', 2, TRUE),
('TVS', 'NTORQ 125', 'Race Edition', 2024, 125, 'PETROL', 'CVT', 'SCOOTER', 2, TRUE),
('TVS', 'XL100', 'Standard', 2024, 99, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE);

-- Bajaj Models
INSERT INTO motorcycle_models (make, model, variant, year, engine_capacity, fuel_type, transmission_type, vehicle_type, seating_capacity, is_active) VALUES
('Bajaj', 'Pulsar N160', 'Standard', 2024, 164, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Bajaj', 'Pulsar NS200', 'Standard', 2024, 199, 'PETROL', 'MANUAL', 'SPORT_BIKE', 2, TRUE),
('Bajaj', 'Pulsar 150', 'Standard', 2024, 149, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Bajaj', 'Dominar 250', 'Standard', 2024, 248, 'PETROL', 'MANUAL', 'CRUISER', 2, TRUE),
('Bajaj', 'Dominar 400', 'Standard', 2024, 373, 'PETROL', 'MANUAL', 'CRUISER', 2, TRUE),
('Bajaj', 'Avenger Cruise 220', 'Standard', 2024, 220, 'PETROL', 'MANUAL', 'CRUISER', 2, TRUE),
('Bajaj', 'CT 110X', 'Standard', 2024, 115, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE),
('Bajaj', 'Platina 110', 'Standard', 2024, 115, 'PETROL', 'MANUAL', 'MOTORCYCLE', 2, TRUE);

-- Suzuki Models
INSERT INTO motorcycle_models (make, model, variant, year, engine_capacity, fuel_type, transmission_type, vehicle_type, seating_capacity, is_active) VALUES
('Suzuki', 'Gixxer', 'Standard', 2024, 155, 'PETROL', 'MANUAL', 'SPORT_BIKE', 2, TRUE),
('Suzuki', 'Gixxer SF', 'Standard', 2024, 155, 'PETROL', 'MANUAL', 'SPORT_BIKE', 2, TRUE),
('Suzuki', 'Gixxer 250', 'Standard', 2024, 249, 'PETROL', 'MANUAL', 'SPORT_BIKE', 2, TRUE),
('Suzuki', 'Access 125', 'Standard', 2024, 124, 'PETROL', 'CVT', 'SCOOTER', 2, TRUE),
('Suzuki', 'Burgman Street', 'Standard', 2024, 125, 'PETROL', 'CVT', 'SCOOTER', 2, TRUE),
('Suzuki', 'Avenis 125', 'Standard', 2024, 124, 'PETROL', 'CVT', 'SCOOTER', 2, TRUE),
('Suzuki', 'V-Strom SX', 'Standard', 2024, 249, 'PETROL', 'MANUAL', 'OFF_ROAD', 2, TRUE);

-- KTM Models
INSERT INTO motorcycle_models (make, model, variant, year, engine_capacity, fuel_type, transmission_type, vehicle_type, seating_capacity, is_active) VALUES
('KTM', 'Duke 125', 'Standard', 2024, 125, 'PETROL', 'MANUAL', 'SPORT_BIKE', 1, TRUE),
('KTM', 'Duke 200', 'Standard', 2024, 199, 'PETROL', 'MANUAL', 'SPORT_BIKE', 1, TRUE),
('KTM', 'Duke 250', 'Standard', 2024, 248, 'PETROL', 'MANUAL', 'SPORT_BIKE', 1, TRUE),
('KTM', 'RC 200', 'Standard', 2024, 199, 'PETROL', 'MANUAL', 'SPORT_BIKE', 1, TRUE),
('KTM', 'RC 390', 'Standard', 2024, 373, 'PETROL', 'MANUAL', 'SPORT_BIKE', 1, TRUE),
('KTM', 'Adventure 250', 'Standard', 2024, 248, 'PETROL', 'MANUAL', 'OFF_ROAD', 2, TRUE),
('KTM', 'Adventure 390', 'Standard', 2024, 373, 'PETROL', 'MANUAL', 'OFF_ROAD', 2, TRUE);

-- Electric Vehicles
INSERT INTO motorcycle_models (make, model, variant, year, engine_capacity, fuel_type, transmission_type, vehicle_type, seating_capacity, is_active) VALUES
('Ather', '450X', 'Standard', 2024, 0, 'ELECTRIC', 'AUTOMATIC', 'SCOOTER', 2, TRUE),
('Ather', '450S', 'Standard', 2024, 0, 'ELECTRIC', 'AUTOMATIC', 'SCOOTER', 2, TRUE),
('Ola Electric', 'S1 Pro', 'Standard', 2024, 0, 'ELECTRIC', 'AUTOMATIC', 'SCOOTER', 2, TRUE),
('Ola Electric', 'S1 Air', 'Standard', 2024, 0, 'ELECTRIC', 'AUTOMATIC', 'SCOOTER', 2, TRUE),
('TVS', 'iQube Electric', 'Standard', 2024, 0, 'ELECTRIC', 'AUTOMATIC', 'SCOOTER', 2, TRUE),
('Bajaj', 'Chetak', 'Standard', 2024, 0, 'ELECTRIC', 'AUTOMATIC', 'SCOOTER', 2, TRUE),
('Revolt', 'RV400', 'Standard', 2024, 0, 'ELECTRIC', 'AUTOMATIC', 'MOTORCYCLE', 2, TRUE);

-- =====================================================
-- Seed: Sample Motorcycles (for testing)
-- =====================================================

-- Get storage location ID for reference
SET @storage_id = (SELECT id FROM storage_locations LIMIT 1);

-- Honda Motorcycles
INSERT INTO motorcycles (vin_number, registration_number, engine_number, chassis_number, motorcycle_model_id, color, mileage_km, manufacture_year, registration_date, status, storage_location_id, purchase_price, purchase_date, selling_price, minimum_price, previous_owners, is_financed, is_accidental, description) 
VALUES
('MHHE1234ABC567890', 'DL01AB1234', 'ENG12345ABC', 'CHS12345ABC', 
 (SELECT id FROM motorcycle_models WHERE make='Honda' AND model='Activa 6G' AND variant='Standard' LIMIT 1),
 'Blue', 12500, 2022, '2022-03-15', 'AVAILABLE', @storage_id, 58000.00, '2025-01-10', 65000.00, 60000.00, 1, FALSE, FALSE, 
 'Well maintained Honda Activa 6G in excellent condition. Single owner, regular service history.'),

('MHHE2345BCD678901', 'DL02CD2345', 'ENG23456BCD', 'CHS23456BCD',
 (SELECT id FROM motorcycle_models WHERE make='Honda' AND model='CB Shine' AND variant='Standard' LIMIT 1),
 'Black', 8500, 2023, '2023-06-20', 'AVAILABLE', @storage_id, 68000.00, '2025-01-12', 75000.00, 70000.00, 1, FALSE, FALSE,
 'Honda CB Shine in pristine condition. Low mileage, well maintained.');

-- Hero Motorcycles
INSERT INTO motorcycles (vin_number, registration_number, engine_number, chassis_number, motorcycle_model_id, color, mileage_km, manufacture_year, registration_date, status, storage_location_id, purchase_price, purchase_date, selling_price, minimum_price, previous_owners, is_financed, is_accidental, description) 
VALUES
('MHHE3456CDE789012', 'DL03EF3456', 'ENG34567CDE', 'CHS34567CDE',
 (SELECT id FROM motorcycle_models WHERE make='Hero' AND model='Splendor Plus' AND variant='Standard' LIMIT 1),
 'Red', 15000, 2021, '2021-08-10', 'AVAILABLE', @storage_id, 45000.00, '2025-01-08', 52000.00, 48000.00, 1, FALSE, FALSE,
 'Reliable Hero Splendor Plus. Excellent fuel efficiency, well maintained.'),

('MHHE4567DEF890123', 'DL04GH4567', 'ENG45678DEF', 'CHS45678DEF',
 (SELECT id FROM motorcycle_models WHERE make='Hero' AND model='Xtreme 160R' AND variant='Standard' LIMIT 1),
 'White', 5200, 2024, '2024-02-14', 'AVAILABLE', @storage_id, 95000.00, '2025-01-13', 105000.00, 98000.00, 1, FALSE, FALSE,
 'Almost new Hero Xtreme 160R. Sports bike with great performance.');

-- Yamaha Motorcycles
INSERT INTO motorcycles (vin_number, registration_number, engine_number, chassis_number, motorcycle_model_id, color, mileage_km, manufacture_year, registration_date, status, storage_location_id, purchase_price, purchase_date, selling_price, minimum_price, previous_owners, is_financed, is_accidental, description) 
VALUES
('MHYE5678EFG901234', 'DL05IJ5678', 'ENG56789EFG', 'CHS56789EFG',
 (SELECT id FROM motorcycle_models WHERE make='Yamaha' AND model='R15 V4' AND variant='Standard' LIMIT 1),
 'Blue', 3200, 2024, '2024-05-10', 'RESERVED', @storage_id, 158000.00, '2025-01-14', 175000.00, 165000.00, 1, FALSE, FALSE,
 'Yamaha R15 V4 in racing blue. Premium sports bike with VVA technology.'),

('MHYE6789FGH012345', 'DL06KL6789', 'ENG67890FGH', 'CHS67890FGH',
 (SELECT id FROM motorcycle_models WHERE make='Yamaha' AND model='FZ-S FI' AND variant='Version 4.0' LIMIT 1),
 'Black', 7800, 2023, '2023-09-25', 'AVAILABLE', @storage_id, 95000.00, '2025-01-11', 105000.00, 98000.00, 1, FALSE, FALSE,
 'Yamaha FZ-S FI V4.0 with LED lights. Stylish and powerful commuter bike.');

-- Royal Enfield Motorcycles
INSERT INTO motorcycles (vin_number, registration_number, engine_number, chassis_number, motorcycle_model_id, color, mileage_km, manufacture_year, registration_date, status, storage_location_id, purchase_price, purchase_date, selling_price, minimum_price, previous_owners, is_financed, is_accidental, description) 
VALUES
('MHRE7890GHI123456', 'DL07MN7890', 'ENG78901GHI', 'CHS78901GHI',
 (SELECT id FROM motorcycle_models WHERE make='Royal Enfield' AND model='Classic 350' AND variant='Dual Channel ABS' LIMIT 1),
 'Stealth Black', 8500, 2023, '2023-07-15', 'AVAILABLE', @storage_id, 165000.00, '2025-01-09', 185000.00, 175000.00, 1, FALSE, FALSE,
 'Royal Enfield Classic 350 with dual channel ABS. Iconic cruiser motorcycle.'),

('MHRE8901HIJ234567', 'DL08OP8901', 'ENG89012HIJ', 'CHS89012HIJ',
 (SELECT id FROM motorcycle_models WHERE make='Royal Enfield' AND model='Himalayan' AND variant='Standard' LIMIT 1),
 'Rock Red', 12000, 2022, '2022-11-20', 'AVAILABLE', @storage_id, 180000.00, '2025-01-07', 195000.00, 185000.00, 2, FALSE, FALSE,
 'Royal Enfield Himalayan adventure bike. Ready for on-road and off-road adventures.');

-- TVS Motorcycles
INSERT INTO motorcycles (vin_number, registration_number, engine_number, chassis_number, motorcycle_model_id, color, mileage_km, manufacture_year, registration_date, status, storage_location_id, purchase_price, purchase_date, selling_price, minimum_price, previous_owners, is_financed, is_accidental, description) 
VALUES
('MHTV9012IJK345678', 'DL09QR9012', 'ENG90123IJK', 'CHS90123IJK',
 (SELECT id FROM motorcycle_models WHERE make='TVS' AND model='Apache RTR 200' AND variant='4V' LIMIT 1),
 'Racing Red', 6200, 2023, '2023-10-05', 'AVAILABLE', @storage_id, 125000.00, '2025-01-12', 138000.00, 130000.00, 1, FALSE, FALSE,
 'TVS Apache RTR 200 4V. High performance sports bike with race tuned FI engine.'),

('MHTV0123JKL456789', 'DL10ST0123', 'ENG01234JKL', 'CHS01234JKL',
 (SELECT id FROM motorcycle_models WHERE make='TVS' AND model='Jupiter 125' AND variant='Standard' LIMIT 1),
 'Titanium Grey', 4500, 2024, '2024-03-18', 'AVAILABLE', @storage_id, 72000.00, '2025-01-14', 78000.00, 74000.00, 1, FALSE, FALSE,
 'TVS Jupiter 125 family scooter. Comfortable and fuel efficient.');

-- KTM Motorcycles
INSERT INTO motorcycles (vin_number, registration_number, engine_number, chassis_number, motorcycle_model_id, color, mileage_km, manufacture_year, registration_date, status, storage_location_id, purchase_price, purchase_date, selling_price, minimum_price, previous_owners, is_financed, is_accidental, description) 
VALUES
('MHKT1234KLM567890', 'DL11UV1234', 'ENG12345KLM', 'CHS12345KLM',
 (SELECT id FROM motorcycle_models WHERE make='KTM' AND model='Duke 200' AND variant='Standard' LIMIT 1),
 'Orange', 8900, 2023, '2023-04-22', 'AVAILABLE', @storage_id, 162000.00, '2025-01-10', 178000.00, 170000.00, 1, FALSE, FALSE,
 'KTM Duke 200 in signature KTM orange. Ready to race street fighter.'),

('MHKT2345LMN678901', 'DL12WX2345', 'ENG23456LMN', 'CHS23456LMN',
 (SELECT id FROM motorcycle_models WHERE make='KTM' AND model='Adventure 250' AND variant='Standard' LIMIT 1),
 'White', 5600, 2024, '2024-01-15', 'AVAILABLE', @storage_id, 245000.00, '2025-01-13', 265000.00, 255000.00, 1, FALSE, FALSE,
 'KTM Adventure 250. Perfect for touring and off-road adventures.');

-- Electric Scooters
INSERT INTO motorcycles (vin_number, registration_number, engine_number, chassis_number, motorcycle_model_id, color, mileage_km, manufacture_year, registration_date, status, storage_location_id, purchase_price, purchase_date, selling_price, minimum_price, previous_owners, is_financed, is_accidental, description) 
VALUES
('MHAT3456MNO789012', 'DL13YZ3456', 'BAT34567MNO', 'CHS34567MNO',
 (SELECT id FROM motorcycle_models WHERE make='Ather' AND model='450X' AND variant='Standard' LIMIT 1),
 'Space Grey', 2800, 2024, '2024-08-10', 'AVAILABLE', @storage_id, 138000.00, '2025-01-15', 148000.00, 142000.00, 1, FALSE, FALSE,
 'Ather 450X electric scooter. Smart connected features, excellent range.'),

('MHOL4567NOP890123', 'DL14AB4567', 'BAT45678NOP', 'CHS45678NOP',
 (SELECT id FROM motorcycle_models WHERE make='Ola Electric' AND model='S1 Pro' AND variant='Standard' LIMIT 1),
 'Jet Black', 1500, 2024, '2024-11-05', 'AVAILABLE', @storage_id, 125000.00, '2025-01-14', 135000.00, 128000.00, 1, FALSE, FALSE,
 'Ola S1 Pro electric scooter. Fast charging, long range, smart features.');

-- =====================================================
-- Seed: Motorcycle Detailed Specs (for sample motorcycles)
-- =====================================================

-- Honda Activa 6G Specs
INSERT INTO motorcycle_detailed_specs (motorcycle_id, engine_type, max_power_bhp, max_torque_nm, cooling_system, fuel_tank_capacity, claimed_mileage_kmpl, length_mm, width_mm, height_mm, wheelbase_mm, ground_clearance_mm, kerb_weight_kg, front_brake_type, rear_brake_type, abs_available, front_suspension, rear_suspension, front_tyre_size, rear_tyre_size, has_electric_start, has_kick_start, has_digital_console, has_usb_charging, has_led_lights)
VALUES
((SELECT id FROM motorcycles WHERE vin_number='MHHE1234ABC567890'),
 'Single Cylinder', 7.68, 8.79, 'AIR_COOLED', 5.3, 60, 1833, 740, 1182, 1260, 171, 109, 'Drum', 'Drum', FALSE, 'Telescopic', 'Spring Loaded Hydraulic', '90/100-10', '90/100-10', TRUE, TRUE, TRUE, TRUE, TRUE);

-- Honda CB Shine Specs
INSERT INTO motorcycle_detailed_specs (motorcycle_id, engine_type, max_power_bhp, max_torque_nm, cooling_system, fuel_tank_capacity, claimed_mileage_kmpl, length_mm, width_mm, height_mm, wheelbase_mm, ground_clearance_mm, kerb_weight_kg, front_brake_type, rear_brake_type, abs_available, front_suspension, rear_suspension, front_tyre_size, rear_tyre_size, has_electric_start, has_kick_start, has_digital_console, has_usb_charging, has_led_lights)
VALUES
((SELECT id FROM motorcycles WHERE vin_number='MHHE2345BCD678901'),
 'Single Cylinder', 10.57, 11.00, 'AIR_COOLED', 10.5, 65, 2017, 738, 1059, 1281, 170, 123, 'Disc', 'Drum', FALSE, 'Telescopic', 'Spring Loaded Hydraulic', '80/100-18', '80/100-18', TRUE, TRUE, FALSE, FALSE, TRUE);

-- Yamaha R15 V4 Specs
INSERT INTO motorcycle_detailed_specs (motorcycle_id, engine_type, max_power_bhp, max_torque_nm, cooling_system, fuel_tank_capacity, claimed_mileage_kmpl, length_mm, width_mm, height_mm, wheelbase_mm, ground_clearance_mm, kerb_weight_kg, front_brake_type, rear_brake_type, abs_available, front_suspension, rear_suspension, front_tyre_size, rear_tyre_size, has_electric_start, has_kick_start, has_digital_console, has_usb_charging, has_led_lights)
VALUES
((SELECT id FROM motorcycles WHERE vin_number='MHYE5678EFG901234'),
 'Single Cylinder VVA', 18.4, 14.2, 'LIQUID_COOLED', 11, 40, 1990, 725, 1135, 1325, 170, 142, 'Disc', 'Disc', TRUE, 'USD Fork', 'Link Type Monoshock', '100/80-17', '140/70-17', TRUE, FALSE, TRUE, TRUE, TRUE);

-- Royal Enfield Classic 350 Specs
INSERT INTO motorcycle_detailed_specs (motorcycle_id, engine_type, max_power_bhp, max_torque_nm, cooling_system, fuel_tank_capacity, claimed_mileage_kmpl, length_mm, width_mm, height_mm, wheelbase_mm, ground_clearance_mm, kerb_weight_kg, front_brake_type, rear_brake_type, abs_available, front_suspension, rear_suspension, front_tyre_size, rear_tyre_size, has_electric_start, has_kick_start, has_digital_console, has_usb_charging, has_led_lights)
VALUES
((SELECT id FROM motorcycles WHERE vin_number='MHRE7890GHI123456'),
 'Single Cylinder', 20.2, 27, 'AIR_OIL_COOLED', 13, 35, 2160, 790, 1110, 1370, 170, 195, 'Disc', 'Disc', TRUE, 'Telescopic', 'Twin Gas Charged Shock Absorbers', '100/90-19', '120/80-18', TRUE, TRUE, FALSE, TRUE, TRUE);

-- KTM Duke 200 Specs
INSERT INTO motorcycle_detailed_specs (motorcycle_id, engine_type, max_power_bhp, max_torque_nm, cooling_system, fuel_tank_capacity, claimed_mileage_kmpl, length_mm, width_mm, height_mm, wheelbase_mm, ground_clearance_mm, kerb_weight_kg, front_brake_type, rear_brake_type, abs_available, front_suspension, rear_suspension, front_tyre_size, rear_tyre_size, has_electric_start, has_kick_start, has_digital_console, has_usb_charging, has_led_lights)
VALUES
((SELECT id FROM motorcycles WHERE vin_number='MHKT1234KLM567890'),
 'Single Cylinder', 25, 19.3, 'LIQUID_COOLED', 13.5, 35, 2020, 830, 1135, 1357, 185, 154, 'Disc', 'Disc', TRUE, 'USD Fork', 'Monoshock', '110/70-17', '150/60-17', TRUE, FALSE, TRUE, FALSE, TRUE);
