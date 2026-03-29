package com.wheelshiftpro.enums;

/**
 * Severity level assigned to an audit event.
 *
 * <ul>
 *   <li>{@link #INFO}     – read-only / informational system events</li>
 *   <li>{@link #REGULAR}  – routine write operations (create, update, move)</li>
 *   <li>{@link #HIGH}     – impactful operations (delete, status change)</li>
 *   <li>{@link #CRITICAL} – sensitive operations (financial, employee, security)</li>
 * </ul>
 */
public enum AuditLevel {
    INFO,
    REGULAR,
    HIGH,
    CRITICAL
}
