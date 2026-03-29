-- V7: Align notification template names with NotificationEventType enum convention
-- Template names must match NotificationEventType.getTemplateName() = enum.name().toLowerCase()

-- reservation_expiring_soon → reservation_expiring  (matches RESERVATION_EXPIRING)
UPDATE notification_templates
SET name = 'reservation_expiring'
WHERE name = 'reservation_expiring_soon' AND channel = 'IN_APP';

-- sale_recorded → sale_completed  (matches SALE_COMPLETED)
UPDATE notification_templates
SET name = 'sale_completed'
WHERE name = 'sale_recorded' AND channel = 'IN_APP';

-- task_overdue stays as-is (matches TASK_OVERDUE)
-- inquiry_assigned stays as-is (matches INQUIRY_ASSIGNED)
-- reservation_created stays as-is (matches RESERVATION_CREATED)
-- inspection_due stays as-is (matches INSPECTION_DUE)
-- task_assigned stays as-is (matches TASK_ASSIGNED)

-- Update existing notification_preferences event_type values from legacy dot-notation to enum names.
-- The only seeded value is 'ALL' which already matches; this handles any manually inserted rows.
UPDATE notification_preferences
SET event_type = UPPER(REPLACE(event_type, '.', '_'))
WHERE event_type LIKE '%.%';
