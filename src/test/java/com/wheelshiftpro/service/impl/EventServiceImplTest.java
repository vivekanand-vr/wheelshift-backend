package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.EventRequest;
import com.wheelshiftpro.dto.response.EventResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Event;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.CarStatus;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.EventMapper;
import com.wheelshiftpro.repository.CarRepository;
import com.wheelshiftpro.repository.ClientRepository;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.EventRepository;
import com.wheelshiftpro.repository.MotorcycleRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventServiceImpl Tests")
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private MotorcycleRepository motorcycleRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private EventServiceImpl eventService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void setUpAuthenticatedEmployee(Long employeeId) {
        Employee e = new Employee();
        e.setId(employeeId);
        EmployeeUserDetails principal = mock(EmployeeUserDetails.class);
        when(principal.getId()).thenReturn(employeeId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
        when(employeeRepository.getReferenceById(employeeId)).thenReturn(e);
    }

    @Nested
    @DisplayName("createEvent")
    class CreateEvent {

        @Test
        @DisplayName("should create event successfully with valid times")
        void happyPath() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(2);
            
            EventRequest request = EventRequest.builder()
                    .title("Meeting")
                    .type("MEETING")
                    .startTime(start)
                    .endTime(end)
                    .build();

            Event event = new Event();
            Event saved = new Event();
            saved.setId(100L);
            saved.setTitle("Meeting");

            EventResponse response = EventResponse.builder()
                    .id(100L)
                    .title("Meeting")
                    .build();

            when(eventMapper.toEntity(request)).thenReturn(event);
            when(eventRepository.save(event)).thenReturn(saved);
            when(eventMapper.toResponse(saved)).thenReturn(response);

            setUpAuthenticatedEmployee(50L);

            EventResponse result = eventService.createEvent(request);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
            verify(eventRepository).save(event);
            verify(auditService).log(eq(AuditCategory.SYSTEM), eq(100L), eq("CREATE"), 
                    eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("should throw exception when endTime is before startTime")
        void endTimeBeforeStart() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.minusHours(1);

            EventRequest request = EventRequest.builder()
                    .title("Meeting")
                    .startTime(start)
                    .endTime(end)
                    .build();

            assertThatThrownBy(() -> eventService.createEvent(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("End time must be after start time");

            verify(eventRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when both carId and motorcycleId are provided")
        void bothVehiclesProvided() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            
            EventRequest request = EventRequest.builder()
                    .title("Test Drive")
                    .type("TEST_DRIVE")
                    .startTime(start)
                    .carId(10L)
                    .motorcycleId(20L)
                    .build();

            assertThatThrownBy(() -> eventService.createEvent(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("one vehicle");

            verify(eventRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when car not found")
        void carNotFound() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            
            EventRequest request = EventRequest.builder()
                    .title("Test Drive")
                    .startTime(start)
                    .carId(999L)
                    .build();

            when(carRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.createEvent(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Car")
                    .hasMessageContaining("999");

            verify(eventRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when motorcycle not found")
        void motorcycleNotFound() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            
            EventRequest request = EventRequest.builder()
                    .title("Test Drive")
                    .startTime(start)
                    .motorcycleId(999L)
                    .build();

            when(motorcycleRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> eventService.createEvent(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Motorcycle")
                    .hasMessageContaining("999");

            verify(eventRepository, never()).save(any());
        }

        @Test
        @DisplayName("should set car relationship after mapping")
        void carRelationshipWired() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            Long carId = 10L;
            
            EventRequest request = EventRequest.builder()
                    .title("Test Drive")
                    .startTime(start)
                    .carId(carId)
                    .type("MEETING")
                    .build();

            Car car = new Car();
            car.setId(carId);
            car.setStatus(CarStatus.AVAILABLE);

            Event event = new Event();
            Event saved = new Event();
            saved.setId(100L);

            when(carRepository.findById(carId)).thenReturn(Optional.of(car));
            when(eventMapper.toEntity(request)).thenReturn(event);
            when(eventRepository.save(event)).thenReturn(saved);
            when(eventMapper.toResponse(saved)).thenReturn(new EventResponse());

            eventService.createEvent(request);

            // Verify car was set on the event
            assertThat(event.getCar()).isEqualTo(car);
        }

        @Test
        @DisplayName("should update car to RESERVED for TEST_DRIVE event when AVAILABLE")
        void testDriveReservesCar() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            Long carId = 10L;
            
            EventRequest request = EventRequest.builder()
                    .title("Test Drive")
                    .type("TEST_DRIVE")
                    .startTime(start)
                    .carId(carId)
                    .build();

            Car car = new Car();
            car.setId(carId);
            car.setStatus(CarStatus.AVAILABLE);

            Event event = new Event();
            Event saved = new Event();
            saved.setId(100L);

            when(carRepository.findById(carId)).thenReturn(Optional.of(car));
            when(eventMapper.toEntity(request)).thenReturn(event);
            when(eventRepository.save(event)).thenReturn(saved);
            when(eventMapper.toResponse(saved)).thenReturn(new EventResponse());

            eventService.createEvent(request);

            assertThat(car.getStatus()).isEqualTo(CarStatus.RESERVED);
            verify(carRepository).save(car);
        }

        @Test
        @DisplayName("should not update car status for TEST_DRIVE when already RESERVED")
        void testDriveSkipsReservedCar() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            Long carId = 10L;
            
            EventRequest request = EventRequest.builder()
                    .title("Test Drive")
                    .type("TEST_DRIVE")
                    .startTime(start)
                    .carId(carId)
                    .build();

            Car car = new Car();
            car.setId(carId);
            car.setStatus(CarStatus.RESERVED);

            Event event = new Event();
            Event saved = new Event();
            saved.setId(100L);

            when(carRepository.findById(carId)).thenReturn(Optional.of(car));
            when(eventMapper.toEntity(request)).thenReturn(event);
            when(eventRepository.save(event)).thenReturn(saved);
            when(eventMapper.toResponse(saved)).thenReturn(new EventResponse());

            eventService.createEvent(request);

            // Car status should remain RESERVED, not changed
            assertThat(car.getStatus()).isEqualTo(CarStatus.RESERVED);
            verify(carRepository, never()).save(car);
        }

        @Test
        @DisplayName("should not update car status for TEST_DRIVE when already SOLD")
        void testDriveSkipsSoldCar() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            Long carId = 10L;
            
            EventRequest request = EventRequest.builder()
                    .title("Test Drive")
                    .type("tEsT_dRiVe")  // Testing case-insensitive matching
                    .startTime(start)
                    .carId(carId)
                    .build();

            Car car = new Car();
            car.setId(carId);
            car.setStatus(CarStatus.SOLD);

            Event event = new Event();
            Event saved = new Event();
            saved.setId(100L);

            when(carRepository.findById(carId)).thenReturn(Optional.of(car));
            when(eventMapper.toEntity(request)).thenReturn(event);
            when(eventRepository.save(event)).thenReturn(saved);
            when(eventMapper.toResponse(saved)).thenReturn(new EventResponse());

            eventService.createEvent(request);

            // Car status should remain SOLD, not changed
            assertThat(car.getStatus()).isEqualTo(CarStatus.SOLD);
            verify(carRepository, never()).save(car);
        }

        @Test
        @DisplayName("should not update car status for non-TEST_DRIVE events")
        void nonTestDriveDoesNotReserve() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            Long carId = 10L;
            
            EventRequest request = EventRequest.builder()
                    .title("Meeting")
                    .type("MEETING")
                    .startTime(start)
                    .carId(carId)
                    .build();

            Car car = new Car();
            car.setId(carId);
            car.setStatus(CarStatus.AVAILABLE);

            Event event = new Event();
            Event saved = new Event();
            saved.setId(100L);

            when(carRepository.findById(carId)).thenReturn(Optional.of(car));
            when(eventMapper.toEntity(request)).thenReturn(event);
            when(eventRepository.save(event)).thenReturn(saved);
            when(eventMapper.toResponse(saved)).thenReturn(new EventResponse());

            eventService.createEvent(request);

            // Car status should remain AVAILABLE, not changed for non-test-drive events
            assertThat(car.getStatus()).isEqualTo(CarStatus.AVAILABLE);
            verify(carRepository, never()).save(car);
        }
    }

    @Nested
    @DisplayName("updateEvent")
    class UpdateEvent {

        @Test
        @DisplayName("should update event successfully")
        void happyPath() {
            Long eventId = 1L;
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(2);

            EventRequest request = EventRequest.builder()
                    .title("Updated Meeting")
                    .startTime(start)
                    .endTime(end)
                    .build();

            Event event = new Event();
            event.setId(eventId);

            EventResponse response = EventResponse.builder()
                    .id(eventId)
                    .title("Updated Meeting")
                    .build();

            when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
            when(eventRepository.save(event)).thenReturn(event);
            when(eventMapper.toResponse(event)).thenReturn(response);

            setUpAuthenticatedEmployee(50L);

            EventResponse result = eventService.updateEvent(eventId, request);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Updated Meeting");
            verify(eventMapper).updateEntityFromRequest(request, event);
            verify(auditService).log(eq(AuditCategory.SYSTEM), eq(eventId), eq("UPDATE"), 
                    eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("should throw exception when event not found")
        void eventNotFound() {
            EventRequest request = EventRequest.builder().build();

            when(eventRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.updateEvent(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Event");

            verify(eventRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when endTime is before startTime")
        void endTimeBeforeStart() {
            Long eventId = 1L;
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.minusHours(1);

            EventRequest request = EventRequest.builder()
                    .startTime(start)
                    .endTime(end)
                    .build();

            Event event = new Event();
            when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.updateEvent(eventId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("End time must be after start time");

            verify(eventRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteEvent")
    class DeleteEvent {

        @Test
        @DisplayName("should delete event successfully")
        void happyPath() {
            Long eventId = 1L;
            Event event = new Event();
            event.setId(eventId);
            event.setTitle("Meeting");

            when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

            setUpAuthenticatedEmployee(50L);

            eventService.deleteEvent(eventId);

            ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
            verify(auditService).log(eq(AuditCategory.SYSTEM), eq(eventId), eq("DELETE"), 
                    eq(AuditLevel.HIGH), any(), detailsCaptor.capture());
            assertThat(detailsCaptor.getValue()).contains("Meeting");
            verify(eventRepository).delete(event);
        }

        @Test
        @DisplayName("should throw exception when event not found")
        void eventNotFound() {
            when(eventRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.deleteEvent(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Event");

            verify(eventRepository, never()).delete(any());
        }
    }
}
