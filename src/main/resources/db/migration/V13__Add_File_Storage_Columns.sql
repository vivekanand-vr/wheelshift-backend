-- V12__Add_File_Storage_Columns.sql
-- Add file storage columns to all entities in WheelShift Pro
-- Stores file IDs (UUIDs) instead of direct URLs for flexibility (supports S3 migration)

-- ============================================================================
-- CAR MODELS - Single representative image
-- ============================================================================
ALTER TABLE car_models
ADD COLUMN model_image_id VARCHAR(64) COMMENT 'File ID for model representative image';

CREATE INDEX idx_car_models_image ON car_models(model_image_id);

-- ============================================================================
-- MOTORCYCLE MODELS - Single representative image
-- ============================================================================
ALTER TABLE motorcycle_models
ADD COLUMN model_image_id VARCHAR(64) COMMENT 'File ID for model representative image';

CREATE INDEX idx_motorcycle_models_image ON motorcycle_models(model_image_id);

-- ============================================================================
-- CARS - Primary image + gallery + documents
-- ============================================================================
ALTER TABLE cars
ADD COLUMN primary_image_id VARCHAR(64) COMMENT 'File ID for primary/featured car image',
ADD COLUMN gallery_image_ids TEXT COMMENT 'Comma-separated file IDs for car gallery images',
ADD COLUMN document_file_ids TEXT COMMENT 'Comma-separated file IDs for car documents (RC, insurance, etc.)';

CREATE INDEX idx_cars_primary_image ON cars(primary_image_id);

-- ============================================================================
-- MOTORCYCLES - Primary image + gallery + documents
-- ============================================================================
ALTER TABLE motorcycles
ADD COLUMN primary_image_id VARCHAR(64) COMMENT 'File ID for primary/featured motorcycle image',
ADD COLUMN gallery_image_ids TEXT COMMENT 'Comma-separated file IDs for motorcycle gallery images',
ADD COLUMN document_file_ids TEXT COMMENT 'Comma-separated file IDs for motorcycle documents (RC, insurance, PUC, etc.)';

CREATE INDEX idx_motorcycles_primary_image ON motorcycles(primary_image_id);

-- ============================================================================
-- CAR INSPECTIONS - Images + inspection report
-- ============================================================================
ALTER TABLE car_inspections
ADD COLUMN inspection_image_ids TEXT COMMENT 'Comma-separated file IDs for inspection photos',
ADD COLUMN inspection_report_file_id VARCHAR(64) COMMENT 'File ID for detailed inspection report PDF';

CREATE INDEX idx_car_inspections_report ON car_inspections(inspection_report_file_id);

-- ============================================================================
-- MOTORCYCLE INSPECTIONS - Images + inspection report
-- ============================================================================
ALTER TABLE motorcycle_inspections
ADD COLUMN inspection_image_ids TEXT COMMENT 'Comma-separated file IDs for inspection photos',
ADD COLUMN inspection_report_file_id VARCHAR(64) COMMENT 'File ID for detailed inspection report PDF';

CREATE INDEX idx_motorcycle_inspections_report ON motorcycle_inspections(inspection_report_file_id);

-- ============================================================================
-- EMPLOYEES - Profile image
-- ============================================================================
ALTER TABLE employees
ADD COLUMN profile_image_id VARCHAR(64) COMMENT 'File ID for employee profile photo';

CREATE INDEX idx_employees_profile_image ON employees(profile_image_id);

-- ============================================================================
-- CLIENTS - Profile image + documents
-- ============================================================================
ALTER TABLE clients
ADD COLUMN profile_image_id VARCHAR(64) COMMENT 'File ID for client profile photo',
ADD COLUMN document_file_ids TEXT COMMENT 'Comma-separated file IDs for client documents (ID proof, address proof, etc.)';

CREATE INDEX idx_clients_profile_image ON clients(profile_image_id);

-- ============================================================================
-- STORAGE LOCATIONS - Location image
-- ============================================================================
ALTER TABLE storage_locations
ADD COLUMN location_image_id VARCHAR(64) COMMENT 'File ID for storage location photo';

CREATE INDEX idx_storage_locations_image ON storage_locations(location_image_id);

-- ============================================================================
-- SALES - Sale documents
-- ============================================================================
ALTER TABLE sales
ADD COLUMN sale_document_ids TEXT COMMENT 'Comma-separated file IDs for sale documents (invoice, receipt, agreement, etc.)';

-- Note: Removed old documents_url column reference, keeping TEXT type for multiple documents

-- ============================================================================
-- FINANCIAL TRANSACTIONS - Receipt/invoice files
-- ============================================================================
ALTER TABLE financial_transactions
ADD COLUMN transaction_file_ids TEXT COMMENT 'Comma-separated file IDs for transaction receipts, invoices, etc.';

-- Note: Removed old receipt_url column reference, keeping TEXT type for multiple files

-- ============================================================================
-- EVENTS - Event attachments
-- ============================================================================
ALTER TABLE events
ADD COLUMN attachment_file_ids TEXT COMMENT 'Comma-separated file IDs for event attachments (images, PDFs, etc.)';

-- ============================================================================
-- INQUIRIES - Inquiry attachments
-- ============================================================================
ALTER TABLE inquiries
ADD COLUMN attachment_file_ids TEXT COMMENT 'Comma-separated file IDs for inquiry attachments';

-- ============================================================================
-- RESERVATIONS - Reservation documents
-- ============================================================================
ALTER TABLE reservations
ADD COLUMN reservation_document_ids TEXT COMMENT 'Comma-separated file IDs for reservation documents (deposit receipt, agreement, etc.)';

-- ============================================================================
-- TASKS - Task attachments
-- ============================================================================
ALTER TABLE tasks
ADD COLUMN attachment_file_ids TEXT COMMENT 'Comma-separated file IDs for task attachments';

-- ============================================================================
-- Comments for Migration
-- ============================================================================
-- This migration adds file storage columns across all entities
-- File IDs are UUIDs that reference the file_metadata table
-- Using file IDs instead of URLs allows:
--   1. Easy migration from local storage to S3 or any cloud provider
--   2. Centralized file metadata management
--   3. File access control and auditing
--   4. Automatic URL generation based on storage backend
-- 
-- Multiple file IDs are stored as comma-separated TEXT for simplicity
-- Alternative: Create junction tables for many-to-many relationships
-- Current approach is simpler and sufficient for most use cases