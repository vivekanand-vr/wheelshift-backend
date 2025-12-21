package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.NotificationPreferenceRequest;
import com.wheelshiftpro.dto.response.NotificationPreferenceResponse;
import com.wheelshiftpro.enums.PrincipalType;
import com.wheelshiftpro.service.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications/preferences")
@RequiredArgsConstructor
@Tag(name = "Notification Preferences", description = "Notification preference management endpoints")
public class NotificationPreferenceController {
    
    private final NotificationPreferenceService preferenceService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Create or update preference", description = "Creates or updates a notification preference")
    public ResponseEntity<NotificationPreferenceResponse> createOrUpdatePreference(
            @Valid @RequestBody NotificationPreferenceRequest request) {
        NotificationPreferenceResponse response = preferenceService.createOrUpdatePreference(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get preference by ID", description = "Retrieves a notification preference by ID")
    public ResponseEntity<NotificationPreferenceResponse> getPreferenceById(@PathVariable Long id) {
        NotificationPreferenceResponse preference = preferenceService.getPreferenceById(id);
        return ResponseEntity.ok(preference);
    }
    
    @GetMapping("/principal/{principalType}/{principalId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get preferences for principal", description = "Retrieves all notification preferences for a principal")
    public ResponseEntity<List<NotificationPreferenceResponse>> getPreferencesForPrincipal(
            @PathVariable PrincipalType principalType,
            @PathVariable Long principalId) {
        List<NotificationPreferenceResponse> preferences = preferenceService.getPreferencesForPrincipal(
                principalType, principalId);
        return ResponseEntity.ok(preferences);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get all preferences", description = "Retrieves all notification preferences")
    public ResponseEntity<Page<NotificationPreferenceResponse>> getAllPreferences(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationPreferenceResponse> preferences = preferenceService.getAllPreferences(pageable);
        return ResponseEntity.ok(preferences);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Delete preference", description = "Deletes a notification preference")
    public ResponseEntity<Void> deletePreference(@PathVariable Long id) {
        preferenceService.deletePreference(id);
        return ResponseEntity.noContent().build();
    }
}
