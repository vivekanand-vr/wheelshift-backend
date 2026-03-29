-- V23__Add_Notification_Digest_Support.sql
-- Adds optimized index for digest scheduler and documents delivery rules implementation
--
-- This migration supports the notification delivery rules features:
-- - Opt-out preferences (checked at job creation)
-- - Severity threshold filtering (checked at job creation)
-- - Digest batching (via SCHEDULED status + scheduled_for timestamp)
-- - Quiet hours enforcement (consumer reschedules jobs during quiet periods)
--
-- The digest scheduler runs hourly and queries for SCHEDULED jobs due for delivery.
-- A compound index on (status, scheduled_for) optimizes this query pattern.

-- Drop the existing single-column index if it exists
DROP INDEX IF EXISTS idx_scheduled ON notification_jobs;

-- Create optimized compound index for digest scheduler query
CREATE INDEX idx_notification_jobs_digest 
ON notification_jobs(status, scheduled_for)
COMMENT 'Optimizes digest scheduler query: WHERE status=SCHEDULED AND scheduled_for <= NOW()';

-- Add comment to scheduled_for column for documentation
ALTER TABLE notification_jobs 
MODIFY COLUMN scheduled_for DATETIME NULL 
COMMENT 'For digest/delayed notifications. Jobs with status=SCHEDULED are processed by NotificationDigestScheduler';
