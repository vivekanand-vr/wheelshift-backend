package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.NotificationTemplateRequest;
import com.wheelshiftpro.dto.response.NotificationTemplateResponse;
import com.wheelshiftpro.enums.NotificationChannel;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.NotificationTemplateService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications/templates")
@RequiredArgsConstructor
@Tag(name = "Notification Templates", description = "Notification template management endpoints")
public class NotificationTemplateController {
    
    private final NotificationTemplateService templateService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Create template", description = "Creates a new notification template")
    public ResponseEntity<NotificationTemplateResponse> createTemplate(
            @Valid @RequestBody NotificationTemplateRequest request,
            Authentication authentication) {
        EmployeeUserDetails userDetails = (EmployeeUserDetails) authentication.getPrincipal();
        Long employeeId = userDetails.getId();
        NotificationTemplateResponse response = templateService.createTemplate(request, employeeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get template by ID", description = "Retrieves a notification template by ID")
    public ResponseEntity<NotificationTemplateResponse> getTemplateById(@PathVariable Long id) {
        NotificationTemplateResponse template = templateService.getTemplateById(id);
        return ResponseEntity.ok(template);
    }
    
    @GetMapping("/latest")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get latest template", description = "Retrieves the latest version of a template")
    public ResponseEntity<NotificationTemplateResponse> getLatestTemplate(
            @RequestParam String name,
            @RequestParam NotificationChannel channel,
            @RequestParam(defaultValue = "en") String locale) {
        NotificationTemplateResponse template = templateService.getLatestTemplate(name, channel, locale);
        return ResponseEntity.ok(template);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get all templates", description = "Retrieves all notification templates")
    public ResponseEntity<Page<NotificationTemplateResponse>> getAllTemplates(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationTemplateResponse> templates = templateService.getAllTemplates(pageable);
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/channel/{channel}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get templates by channel", description = "Retrieves templates for a specific channel")
    public ResponseEntity<Page<NotificationTemplateResponse>> getTemplatesByChannel(
            @PathVariable NotificationChannel channel,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationTemplateResponse> templates = templateService.getTemplatesByChannel(channel, pageable);
        return ResponseEntity.ok(templates);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Update template", description = "Updates a notification template")
    public ResponseEntity<NotificationTemplateResponse> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody NotificationTemplateRequest request) {
        NotificationTemplateResponse template = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(template);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete template", description = "Deletes a notification template")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
