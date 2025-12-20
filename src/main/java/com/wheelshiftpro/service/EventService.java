package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.EventRequest;
import com.wheelshiftpro.dto.response.EventResponse;
import com.wheelshiftpro.dto.response.PageResponse;

import java.time.LocalDateTime;

/**
 * Service interface for calendar event management operations.
 */
public interface EventService {

    /**
     * Creates a new calendar event.
     *
     * @param request the event creation request
     * @return the created event response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if employee or client not found
     */
    EventResponse createEvent(EventRequest request);

    /**
     * Updates an existing event.
     *
     * @param id the event ID
     * @param request the update request
     * @return the updated event response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if event not found
     */
    EventResponse updateEvent(Long id, EventRequest request);

    /**
     * Retrieves an event by ID.
     *
     * @param id the event ID
     * @return the event response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if event not found
     */
    EventResponse getEventById(Long id);

    /**
     * Retrieves all events with pagination.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return paginated event responses
     */
    PageResponse<EventResponse> getAllEvents(int page, int size);

    /**
     * Deletes an event.
     *
     * @param id the event ID
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if event not found
     */
    void deleteEvent(Long id);

    /**
     * Retrieves events for a specific employee.
     *
     * @param employeeId the employee ID
     * @param page the page number
     * @param size the page size
     * @return paginated employee events
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if employee not found
     */
    PageResponse<EventResponse> getEventsByEmployee(Long employeeId, int page, int size);

    /**
     * Retrieves events for a specific client.
     *
     * @param clientId the client ID
     * @param page the page number
     * @param size the page size
     * @return paginated client events
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if client not found
     */
    PageResponse<EventResponse> getEventsByClient(Long clientId, int page, int size);

    /**
     * Retrieves events within a date range.
     *
     * @param startDate the start date and time
     * @param endDate the end date and time
     * @param page the page number
     * @param size the page size
     * @return paginated events within the date range
     */
    PageResponse<EventResponse> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate, 
                                                      int page, int size);

    /**
     * Retrieves upcoming events (events starting after current date/time).
     *
     * @param page the page number
     * @param size the page size
     * @return paginated upcoming events
     */
    PageResponse<EventResponse> getUpcomingEvents(int page, int size);

    /**
     * Retrieves events for today.
     *
     * @param page the page number
     * @param size the page size
     * @return paginated today's events
     */
    PageResponse<EventResponse> getTodayEvents(int page, int size);

    /**
     * Searches events by title.
     *
     * @param title the title to search for (partial match)
     * @param page the page number
     * @param size the page size
     * @return paginated search results
     */
    PageResponse<EventResponse> searchEventsByTitle(String title, int page, int size);
}
