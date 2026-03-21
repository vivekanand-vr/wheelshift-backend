package com.wheelshiftpro.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base entity class providing common auditing fields for all entities.
 * Uses JPA auditing via {@link AuditingEntityListener} to automatically
 * populate {@code createdAt} and {@code updatedAt} timestamps.
 *
 * <p>Do NOT add manual {@code @PrePersist} / {@code @PreUpdate} hooks here —
 * the {@link AuditingEntityListener} already handles timestamp population.
 * Duplicate hooks cause double-writes and can overwrite auditing values.
 * Requires {@code @EnableJpaAuditing} on a {@code @Configuration} class
 * (see {@code JpaAuditingConfig}).
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
