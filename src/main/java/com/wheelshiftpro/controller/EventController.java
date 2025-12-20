package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.EventRequest;
import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.EventResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Event Management", description = "APIs for managing events and appointments")
public class EventController {

    private final EventService eventService;

    @PostMapping
    @Operation(summary = "Create a new event", description = "Creates a new event or appointment")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody EventRequest request) {
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an event", description = "Updates an existing event by ID")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @Parameter(description = "Event ID") @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        EventResponse response = eventService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID", description = "Retrieves a specific event by its ID")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(
            @Parameter(description = "Event ID") @PathVariable Long id) {
        EventResponse response = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all events", description = "Retrieves all events with pagination")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> getAllEvents(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<EventResponse> response = eventService.getAllEvents(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an event", description = "Deletes an event by ID")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @Parameter(description = "Event ID") @PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Event deleted successfully", null));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get events by employee", description = "Retrieves all events for a specific employee")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> getEventsByEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<EventResponse> response = eventService.getEventsByEmployee(employeeId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get events by client", description = "Retrieves all events for a specific client")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> getEventsByClient(
            @Parameter(description = "Client ID") @PathVariable Long clientId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<EventResponse> response = eventService.getEventsByClient(clientId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get events by date range", description = "Retrieves all events within a date range")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> getEventsByDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<EventResponse> response = eventService.getEventsByDateRange(
                startDate.atStartOfDay(), endDate.atTime(23, 59, 59), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming events", description = "Retrieves upcoming events")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> getUpcomingEvents(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<EventResponse> response = eventService.getUpcomingEvents(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's events", description = "Retrieves events scheduled for today")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> getTodayEvents(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<EventResponse> response = eventService.getTodayEvents(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search events by title", description = "Search events with title keyword")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> searchEventsByTitle(
            @Parameter(description = "Title keyword") @RequestParam String title,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<EventResponse> response = eventService.searchEventsByTitle(title, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
