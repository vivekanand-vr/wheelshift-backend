package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.ClientRequest;
import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.ClientResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.ClientStatus;
import com.wheelshiftpro.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Client Management", description = "APIs for managing clients and customers")
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SALES')")
    @Operation(summary = "Create a new client", description = "Registers a new client in the system")
    public ResponseEntity<ApiResponse<ClientResponse>> createClient(
            @Valid @RequestBody ClientRequest request) {
        ClientResponse response = clientService.createClient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Client created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SALES')")
    @Operation(summary = "Update a client", description = "Updates an existing client by ID")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClient(
            @Parameter(description = "Client ID") @PathVariable Long id,
            @Valid @RequestBody ClientRequest request) {
        ClientResponse response = clientService.updateClient(id, request);
        return ResponseEntity.ok(ApiResponse.success("Client updated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get client by ID", description = "Retrieves a specific client by their ID")
    public ResponseEntity<ApiResponse<ClientResponse>> getClientById(
            @Parameter(description = "Client ID") @PathVariable Long id) {
        ClientResponse response = clientService.getClientById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all clients", description = "Retrieves all clients with pagination")
    public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> getAllClients(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<ClientResponse> response = clientService.getAllClients(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Delete a client", description = "Deletes a client by ID")
    public ResponseEntity<ApiResponse<Void>> deleteClient(
            @Parameter(description = "Client ID") @PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Client deleted successfully", null));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search clients", description = "Search clients by name, email, or phone")
    public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> searchClients(
            @Parameter(description = "Search term (name, email, or phone)") @RequestParam String searchTerm,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<ClientResponse> response = clientService.searchClients(searchTerm, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get clients by status", description = "Retrieves all clients with a specific status")
    public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> getClientsByStatus(
            @Parameter(description = "Client status") @PathVariable ClientStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<ClientResponse> response = clientService.getClientsByStatus(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/top-buyers")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get top buyers", description = "Retrieves the top client buyers")
    public ResponseEntity<ApiResponse<List<ClientResponse>>> getTopBuyers(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {
        List<ClientResponse> response = clientService.getTopBuyers(limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SALES')")
    @Operation(summary = "Update client status", description = "Updates the status of a client")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClientStatus(
            @Parameter(description = "Client ID") @PathVariable Long id,
            @Parameter(description = "New client status") @RequestParam ClientStatus status) {
        ClientResponse response = clientService.updateClientStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Client status updated successfully", response));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Get client statistics", description = "Retrieves client statistics by status")
    public ResponseEntity<ApiResponse<Object>> getClientStatistics() {
        Object statistics = clientService.getClientStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
}
