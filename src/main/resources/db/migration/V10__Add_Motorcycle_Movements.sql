-- V11__Add_Motorcycle_Movements.sql
-- Create motorcycle_movements table to track motorcycle location changes

CREATE TABLE motorcycle_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Motorcycle Reference
    motorcycle_id BIGINT NOT NULL COMMENT 'Reference to motorcycles table',
    
    -- Location References
    from_location_id BIGINT COMMENT 'Source storage location (NULL for initial placement)',
    to_location_id BIGINT NOT NULL COMMENT 'Destination storage location',
    
    -- Movement Details
    moved_at DATETIME NOT NULL COMMENT 'Date and time of movement',
    moved_by_employee_id BIGINT COMMENT 'Employee who performed the movement',
    notes TEXT COMMENT 'Additional notes about the movement',
    
    -- Foreign Keys
    CONSTRAINT fk_movement_motorcycle 
        FOREIGN KEY (motorcycle_id) REFERENCES motorcycles(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    
    CONSTRAINT fk_motorcycle_from_location 
        FOREIGN KEY (from_location_id) REFERENCES storage_locations(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    
    CONSTRAINT fk_motorcycle_to_location 
        FOREIGN KEY (to_location_id) REFERENCES storage_locations(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    
    CONSTRAINT fk_motorcycle_moved_by_employee 
        FOREIGN KEY (moved_by_employee_id) REFERENCES employees(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    
    -- Indexes
    INDEX idx_motorcycle (motorcycle_id),
    INDEX idx_from_location (from_location_id),
    INDEX idx_to_location (to_location_id),
    INDEX idx_moved_at (moved_at),
    INDEX idx_moved_by (moved_by_employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Motorcycle movement history between storage locations';
