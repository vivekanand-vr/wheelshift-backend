package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.EventRequest;
import com.wheelshiftpro.dto.response.EventResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.entity.Event;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.EventMapper;
import com.wheelshiftpro.repository.CarRepository;
import com.wheelshiftpro.repository.ClientRepository;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.EventRepository;
import com.wheelshiftpro.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CarRepository carRepository;
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final EventMapper eventMapper;

    @Override
    public EventResponse createEvent(EventRequest request) {
        log.debug("Creating event: {}", request.getTitle());

        if (request.getCarId() != null && !carRepository.existsById(request.getCarId())) {
            throw new ResourceNotFoundException("Car", "id", request.getCarId());
        }

        Event event = eventMapper.toEntity(request);
        Event saved = eventRepository.save(event);

        log.info("Created event with ID: {}", saved.getId());
        return eventMapper.toResponse(saved);
    }

    @Override
    public EventResponse updateEvent(Long id, EventRequest request) {
        log.debug("Updating event ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        eventMapper.updateEntityFromRequest(request, event);
        Event updated = eventRepository.save(event);

        log.info("Updated event ID: {}", id);
        return eventMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        log.debug("Fetching event ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getAllEvents(int page, int size) {
        log.debug("Fetching all events - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        Page<Event> eventsPage = eventRepository.findAll(pageable);

        return buildPageResponse(eventsPage);
    }

    @Override
    public void deleteEvent(Long id) {
        log.debug("Deleting event ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        eventRepository.delete(event);
        log.info("Deleted event ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getEventsByEmployee(Long employeeId, int page, int size) {
        log.debug("Fetching events for employee ID: {}", employeeId);

        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        // Note: Event entity doesn't have employee relationship
        // This method should return empty results or be redesigned
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime"));
        Page<Event> eventsPage = Page.empty(pageable);

        return buildPageResponse(eventsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getEventsByClient(Long clientId, int page, int size) {
        log.debug("Fetching events for client ID: {}", clientId);

        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", "id", clientId);
        }

        // Note: Event entity doesn't have client relationship
        // This method should return empty results or be redesigned
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime"));
        Page<Event> eventsPage = Page.empty(pageable);

        return buildPageResponse(eventsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate, 
                                                             int page, int size) {
        log.debug("Fetching events between {} and {}", startDate, endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime"));
        List<Event> eventsList = eventRepository.findEventsBetween(startDate, endDate);
        Page<Event> eventsPage = new PageImpl<>(eventsList, pageable, eventsList.size());

        return buildPageResponse(eventsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getUpcomingEvents(int page, int size) {
        log.debug("Fetching upcoming events");

        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime"));
        List<Event> eventsList = eventRepository.findUpcomingEvents(now, pageable);
        Page<Event> eventsPage = new PageImpl<>(eventsList, pageable, eventsList.size());

        return buildPageResponse(eventsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getTodayEvents(int page, int size) {
        log.debug("Fetching today's events");

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime"));
        List<Event> eventsList = eventRepository.findEventsBetween(startOfDay, endOfDay);
        Page<Event> eventsPage = new PageImpl<>(eventsList, pageable, eventsList.size());

        return buildPageResponse(eventsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EventResponse> searchEventsByTitle(String title, int page, int size) {
        log.debug("Searching events by title: {}", title);

        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        Page<Event> eventsPage = eventRepository.findByTitleContainingIgnoreCase(title, pageable);

        return buildPageResponse(eventsPage);
    }

    private PageResponse<EventResponse> buildPageResponse(Page<Event> page) {
        List<EventResponse> content = eventMapper.toResponseList(page.getContent());

        return PageResponse.<EventResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
