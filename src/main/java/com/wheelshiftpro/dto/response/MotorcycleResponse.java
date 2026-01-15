package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.MotorcycleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for motorcycle response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotorcycleResponse {

    private Long id;
    private String vinNumber;
    private String registrationNumber;
    private String engineNumber;
    private String chassisNumber;
    
    // Motorcycle Model Info (flattened)
    private Long motorcycleModelId;
    private String motorcycleModelFullName;
    private String make;
    private String model;
    private String variant;
    private Integer modelYear;
    
    private String color;
    private Integer mileageKm;
    private Integer manufactureYear;
    private LocalDate registrationDate;
    private MotorcycleStatus status;
    
    // Storage Location (flattened)
    private Long storageLocationId;
    private String storageLocationName;
    
    private BigDecimal purchasePrice;
    private LocalDate purchaseDate;
    private BigDecimal sellingPrice;
    private BigDecimal minimumPrice;
    private Integer previousOwners;
    private LocalDate insuranceExpiryDate;
    private LocalDate pollutionCertificateExpiry;
    private Boolean isFinanced;
    private Boolean isAccidental;
    private String description;
    
    // Computed fields
    private BigDecimal profitMargin;
    private Integer ageInYears;
    private Boolean isInsuranceExpired;
    private Boolean isPollutionCertificateExpired;
    private String fullIdentification;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
