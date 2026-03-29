package com.wheelshiftpro.enums;

/**
 * Domain category of the entity that was affected by an audit event.
 * Used to let roles filter the audit log by the area they are responsible for.
 */
public enum AuditCategory {
    CAR,
    MOTORCYCLE,
    FINANCIAL_TRANSACTION,
    SALE,
    RESERVATION,
    INQUIRY,
    CLIENT,
    EMPLOYEE,
    INSPECTION,
    STORAGE_LOCATION,
    TASK,
    SYSTEM
}
