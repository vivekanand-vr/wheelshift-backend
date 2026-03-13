package com.wheelshiftpro.entity;

import com.wheelshiftpro.enums.ClientStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a client/customer in the system.
 * Tracks client information and purchase history.
 */
@Entity
@Table(name = "clients", uniqueConstraints = @UniqueConstraint(name = "uk_client_email", columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 128, message = "Name must not exceed 128 characters")
    @Column(name = "name", length = 128, nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 128, message = "Email must not exceed 128 characters")
    @Column(name = "email", length = 128, nullable = false, unique = true)
    private String email;

    @Size(max = 32, message = "Phone must not exceed 32 characters")
    @Column(name = "phone", length = 32)
    private String phone;

    @Column(name = "profile_image_id", length = 64)
    private String profileImageId;

    @Column(name = "document_file_ids", columnDefinition = "TEXT")
    private String documentFileIds;

    @Size(max = 128, message = "Location must not exceed 128 characters")
    @Column(name = "location", length = 128)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private ClientStatus status = ClientStatus.ACTIVE;

    @Min(value = 0, message = "Total purchases cannot be negative")
    @Column(name = "total_purchases")
    @Builder.Default
    private Integer totalPurchases = 0;

    @Column(name = "last_purchase")
    private LocalDate lastPurchase;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Inquiry> inquiries = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Sale> sales = new ArrayList<>();
}
