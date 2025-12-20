package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.ClientStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for client response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String location;
    private ClientStatus status;
    private Integer totalPurchases;
    private LocalDate lastPurchase;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
