package com.wheelshiftpro.enums.notifications;

/**
 * All supported notification event types.
 * The enum name (lowercase) is also used as the template lookup key,
 * e.g. INQUIRY_ASSIGNED → template name "inquiry_assigned".
 */
public enum NotificationEventType {

    // ── Inquiry events ────────────────────────────────────────────────────────
    INQUIRY_CREATED,
    INQUIRY_ASSIGNED,
    INQUIRY_UPDATED,

    // ── Reservation events ────────────────────────────────────────────────────
    RESERVATION_CREATED,
    RESERVATION_EXPIRING,
    RESERVATION_CANCELLED,

    // ── Sale events ───────────────────────────────────────────────────────────
    SALE_COMPLETED,
    SALE_PENDING,

    // ── Inspection events ─────────────────────────────────────────────────────
    INSPECTION_DUE,
    INSPECTION_COMPLETED,
    INSPECTION_FAILED,

    // ── Task events ───────────────────────────────────────────────────────────
    TASK_ASSIGNED,
    TASK_DUE,
    TASK_OVERDUE,
    TASK_COMPLETED,

    // ── Payment events ────────────────────────────────────────────────────────
    PAYMENT_RECEIVED,
    PAYMENT_OVERDUE,

    // ── RBAC / security events ────────────────────────────────────────────────
    DATA_SCOPE_CHANGED,

    // ── Wildcard — matches any event type when stored in preferences ──────────
    ALL;

    /**
     * Returns the template name convention used for DB lookups.
     * Converts SCREAMING_SNAKE_CASE to lowercase_snake_case.
     * e.g. INQUIRY_ASSIGNED → "inquiry_assigned"
     */
    public String getTemplateName() {
        return this.name().toLowerCase();
    }
}
