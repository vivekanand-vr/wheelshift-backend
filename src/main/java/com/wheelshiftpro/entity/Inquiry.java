package com.wheelshiftpro.entity;

import com.wheelshiftpro.enums.InquiryStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a client inquiry about a car.
 * Tracks inquiry lifecycle from creation to closure.
 */
@Entity
@Table(name = "inquiries",
       indexes = {
           @Index(name = "idx_inquiry_status_employee", columnList = "status, assigned_employee_id"),
           @Index(name = "idx_inquiry_car_client", columnList = "car_id, client_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", foreignKey = @ForeignKey(name = "fk_inquiry_car"))
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inquiry_client"))
    @NotNull(message = "Client is required")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_employee_id", foreignKey = @ForeignKey(name = "fk_inquiry_employee"))
    private Employee assignedEmployee;

    @Size(max = 64, message = "Inquiry type must not exceed 64 characters")
    @Column(name = "inquiry_type", length = 64)
    private String inquiryType;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private InquiryStatus status = InquiryStatus.OPEN;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Column(name = "response_date")
    private LocalDateTime responseDate;
}
