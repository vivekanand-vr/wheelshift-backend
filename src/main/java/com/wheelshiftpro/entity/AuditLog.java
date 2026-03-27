package com.wheelshiftpro.entity;

import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Immutable audit record for write operations on any domain entity.
 *
 * <p>Records are never updated after creation — there is no {@code updatedAt} field.
 */
@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Domain area of the affected entity (e.g. CAR, MOTORCYCLE, FINANCIAL_TRANSACTION). */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 64)
    private AuditCategory category;

    /** Primary key of the affected entity; null for SYSTEM-level events. */
    @Column(name = "entity_id")
    private Long entityId;

    /** Operation performed, e.g. CREATE, UPDATE, DELETE, MOVE, STATUS_CHANGE. */
    @Column(name = "action", nullable = false, length = 64)
    private String action;

    /** Severity of the event (INFO, REGULAR, HIGH, CRITICAL). */
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 16)
    private AuditLevel level;

    /** Employee who triggered the action; null for system-initiated operations. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_id", foreignKey = @ForeignKey(name = "fk_audit_performed_by"))
    private Employee performedBy;

    /** Optional human-readable summary of what changed. */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
