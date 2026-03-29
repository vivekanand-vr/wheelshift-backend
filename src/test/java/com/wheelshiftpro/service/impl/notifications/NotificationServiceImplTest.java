package com.wheelshiftpro.service.impl.notifications;

import com.wheelshiftpro.dto.request.notifications.CreateNotificationEventRequest;
import com.wheelshiftpro.dto.request.notifications.CreateNotificationJobRequest;
import com.wheelshiftpro.dto.response.notifications.NotificationEventResponse;
import com.wheelshiftpro.dto.response.notifications.NotificationJobResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.notifications.NotificationEvent;
import com.wheelshiftpro.entity.notifications.NotificationJob;
import com.wheelshiftpro.entity.notifications.NotificationTemplate;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.RecipientType;
import com.wheelshiftpro.enums.notifications.NotificationChannel;
import com.wheelshiftpro.enums.notifications.NotificationEventType;
import com.wheelshiftpro.enums.notifications.NotificationSeverity;
import com.wheelshiftpro.enums.notifications.NotificationStatus;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.messaging.NotificationKafkaProducer;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.notifications.NotificationEventRepository;
import com.wheelshiftpro.repository.notifications.NotificationJobRepository;
import com.wheelshiftpro.repository.notifications.NotificationTemplateRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
import com.wheelshiftpro.service.notifications.NotificationTemplateService;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl Tests")
class NotificationServiceImplTest {

    @Mock private NotificationEventRepository eventRepository;
    @Mock private NotificationJobRepository jobRepository;
    @Mock private NotificationTemplateRepository templateRepository;
    @Mock private NotificationTemplateService templateService;
    @Mock private NotificationKafkaProducer kafkaProducer;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private AuditService auditService;

    @InjectMocks private NotificationServiceImpl notificationService;

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
        // lenient — only reached when audit is actually called
        lenient().when(employeeRepository.getReferenceById(employeeId)).thenReturn(e);
    }

    private void setUpAuthenticatedAdmin(Long employeeId) {
        EmployeeUserDetails principal = mock(EmployeeUserDetails.class);
        when(principal.getId()).thenReturn(employeeId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        Employee e = new Employee();
        e.setId(employeeId);
        lenient().when(employeeRepository.getReferenceById(employeeId)).thenReturn(e);
    }

    private NotificationEvent buildEvent(Long id) {
        return NotificationEvent.builder()
                .id(id)
                .eventType(NotificationEventType.INQUIRY_ASSIGNED)
                .entityType("Inquiry")
                .entityId(200L)
                .payload(Map.of("recipientId", "5", "recipientType", "EMPLOYEE"))
                .severity(NotificationSeverity.INFO)
                .occurredAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private NotificationJob buildJob(Long id, Long recipientId) {
        NotificationEvent event = buildEvent(10L);
        return NotificationJob.builder()
                .id(id)
                .event(event)
                .recipientType(RecipientType.EMPLOYEE)
                .recipientId(recipientId)
                .channel(NotificationChannel.IN_APP)
                .status(NotificationStatus.PENDING)
                .dedupKey("10-EMPLOYEE-" + recipientId + "-IN_APP")
                .retries(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createNotificationEvent")
    class CreateNotificationEvent {

        @Test
        @DisplayName("should persist event, generate job from payload, publish to Kafka")
        void happyPath_withRecipientInPayload() {
            CreateNotificationEventRequest request = CreateNotificationEventRequest.builder()
                    .eventType(NotificationEventType.INQUIRY_ASSIGNED)
                    .entityType("Inquiry")
                    .entityId(100L)
                    .payload(Map.of("recipientId", "42", "recipientType", "EMPLOYEE"))
                    .severity(NotificationSeverity.INFO)
                    .occurredAt(LocalDateTime.now())
                    .build();

            NotificationEvent savedEvent = buildEvent(1L);
            when(eventRepository.save(any(NotificationEvent.class))).thenReturn(savedEvent);
            when(jobRepository.findByDedupKey(any())).thenReturn(Optional.empty());
            when(jobRepository.save(any(NotificationJob.class))).thenReturn(buildJob(10L, 42L));

            NotificationEventResponse result = notificationService.createNotificationEvent(request);

            assertThat(result).isNotNull();
            verify(eventRepository).save(any(NotificationEvent.class));
            verify(kafkaProducer).publishJob(any(NotificationJob.class));
            verify(auditService).log(eq(AuditCategory.SYSTEM), eq(1L),
                    eq("CREATE_NOTIFICATION_EVENT"), eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("should persist event without generating a job when no recipient in payload")
        void happyPath_noRecipientInPayload() {
            CreateNotificationEventRequest request = CreateNotificationEventRequest.builder()
                    .eventType(NotificationEventType.RESERVATION_CREATED)
                    .entityType("Reservation")
                    .entityId(50L)
                    .payload(Map.of("someKey", "someValue"))
                    .severity(NotificationSeverity.INFO)
                    .occurredAt(LocalDateTime.now())
                    .build();

            // saved event must also have empty payload so generateNotificationJobs finds no recipientId
            NotificationEvent savedEvent = NotificationEvent.builder()
                    .id(2L)
                    .eventType(NotificationEventType.RESERVATION_CREATED)
                    .entityType("Reservation").entityId(50L)
                    .payload(Map.of("someKey", "someValue"))
                    .severity(NotificationSeverity.INFO)
                    .occurredAt(LocalDateTime.now()).createdAt(LocalDateTime.now())
                    .build();
            when(eventRepository.save(any(NotificationEvent.class))).thenReturn(savedEvent);

            NotificationEventResponse result = notificationService.createNotificationEvent(request);

            assertThat(result).isNotNull();
            verify(kafkaProducer, never()).publishJob(any());
            verify(auditService).log(eq(AuditCategory.SYSTEM), eq(2L),
                    eq("CREATE_NOTIFICATION_EVENT"), eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("should skip job creation when dedup key already exists")
        void dedupKeyExists_noSecondJob() {
            CreateNotificationEventRequest request = CreateNotificationEventRequest.builder()
                    .eventType(NotificationEventType.INQUIRY_ASSIGNED)
                    .entityType("Inquiry")
                    .entityId(1L)
                    .payload(Map.of("recipientId", "5", "recipientType", "EMPLOYEE"))
                    .severity(NotificationSeverity.INFO)
                    .occurredAt(LocalDateTime.now())
                    .build();

            NotificationEvent savedEvent = buildEvent(3L);
            when(eventRepository.save(any())).thenReturn(savedEvent);
            when(jobRepository.findByDedupKey(any())).thenReturn(Optional.of(buildJob(99L, 5L)));

            notificationService.createNotificationEvent(request);

            verify(jobRepository, never()).save(any());
            verify(kafkaProducer, never()).publishJob(any());
        }

        @Test
        @DisplayName("audit level must be REGULAR for event creation")
        void auditLevelRegular() {
            CreateNotificationEventRequest request = CreateNotificationEventRequest.builder()
                    .eventType(NotificationEventType.INQUIRY_ASSIGNED)
                    .entityType("Inquiry").entityId(1L).payload(Map.of())
                    .severity(NotificationSeverity.INFO).occurredAt(LocalDateTime.now()).build();

            when(eventRepository.save(any())).thenReturn(buildEvent(1L));

            notificationService.createNotificationEvent(request);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("audit performedBy is populated when authenticated")
        void auditFieldWhenAuthenticated() {
            setUpAuthenticatedEmployee(77L);

            CreateNotificationEventRequest request = CreateNotificationEventRequest.builder()
                    .eventType(NotificationEventType.INQUIRY_ASSIGNED)
                    .entityType("Inquiry").entityId(1L).payload(Map.of())
                    .severity(NotificationSeverity.INFO).occurredAt(LocalDateTime.now()).build();

            when(eventRepository.save(any())).thenReturn(buildEvent(1L));

            notificationService.createNotificationEvent(request);

            ArgumentCaptor<Employee> empCaptor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), empCaptor.capture(), any());
            assertThat(empCaptor.getValue()).isNotNull();
            assertThat(empCaptor.getValue().getId()).isEqualTo(77L);
        }

        @Test
        @DisplayName("audit performedBy is null when unauthenticated")
        void auditFieldNullWhenUnauthenticated() {
            CreateNotificationEventRequest request = CreateNotificationEventRequest.builder()
                    .eventType(NotificationEventType.INQUIRY_ASSIGNED)
                    .entityType("Inquiry").entityId(1L).payload(Map.of())
                    .severity(NotificationSeverity.INFO).occurredAt(LocalDateTime.now()).build();

            when(eventRepository.save(any())).thenReturn(buildEvent(1L));

            notificationService.createNotificationEvent(request);

            ArgumentCaptor<Employee> empCaptor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), empCaptor.capture(), any());
            assertThat(empCaptor.getValue()).isNull();
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createNotificationJob")
    class CreateNotificationJob {

        @Test
        @DisplayName("should create and persist a new notification job")
        void happyPath() {
            CreateNotificationJobRequest request = CreateNotificationJobRequest.builder()
                    .eventId(10L)
                    .recipientType(RecipientType.EMPLOYEE)
                    .recipientId(5L)
                    .channel(NotificationChannel.IN_APP)
                    .build();

            NotificationEvent event = buildEvent(10L);
            when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
            when(jobRepository.findByDedupKey(any())).thenReturn(Optional.empty());

            NotificationJob saved = buildJob(20L, 5L);
            when(jobRepository.save(any(NotificationJob.class))).thenReturn(saved);

            NotificationJobResponse result = notificationService.createNotificationJob(request);

            assertThat(result).isNotNull();
            verify(jobRepository).save(any(NotificationJob.class));
            verify(auditService).log(eq(AuditCategory.SYSTEM), eq(20L),
                    eq("CREATE_NOTIFICATION_JOB"), eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("should return existing job without saving when dedup key exists")
        void dedupKeyExists_returnsExisting() {
            CreateNotificationJobRequest request = CreateNotificationJobRequest.builder()
                    .eventId(10L).recipientType(RecipientType.EMPLOYEE).recipientId(5L)
                    .channel(NotificationChannel.IN_APP).build();

            NotificationEvent event = buildEvent(10L);
            when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

            NotificationJob existing = buildJob(99L, 5L);
            when(jobRepository.findByDedupKey(any())).thenReturn(Optional.of(existing));

            NotificationJobResponse result = notificationService.createNotificationJob(request);

            assertThat(result).isNotNull();
            verify(jobRepository, never()).save(any());
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when event not found")
        void eventNotFound_throws() {
            CreateNotificationJobRequest request = CreateNotificationJobRequest.builder()
                    .eventId(999L).recipientType(RecipientType.EMPLOYEE).recipientId(5L)
                    .channel(NotificationChannel.IN_APP).build();

            when(eventRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.createNotificationJob(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("NotificationEvent");

            verify(jobRepository, never()).save(any());
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("audit level must be REGULAR for job creation")
        void auditLevelRegular() {
            CreateNotificationJobRequest request = CreateNotificationJobRequest.builder()
                    .eventId(10L).recipientType(RecipientType.EMPLOYEE).recipientId(5L)
                    .channel(NotificationChannel.IN_APP).build();

            when(eventRepository.findById(10L)).thenReturn(Optional.of(buildEvent(10L)));
            when(jobRepository.findByDedupKey(any())).thenReturn(Optional.empty());
            when(jobRepository.save(any())).thenReturn(buildJob(1L, 5L));

            notificationService.createNotificationJob(request);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.REGULAR), any(), any());
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("markNotificationAsRead")
    class MarkNotificationAsRead {

        @Test
        @DisplayName("should mark notification as read when sentAt is null")
        void happyPath() {
            NotificationJob job = buildJob(5L, 42L);
            job.setSentAt(null);

            when(jobRepository.findById(5L)).thenReturn(Optional.of(job));
            when(jobRepository.save(job)).thenReturn(job);

            setUpAuthenticatedEmployee(42L);

            NotificationJobResponse result = notificationService.markNotificationAsRead(5L);

            assertThat(result).isNotNull();
            assertThat(job.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(job.getSentAt()).isNotNull();
            verify(jobRepository).save(job);
            verify(auditService).log(eq(AuditCategory.SYSTEM), eq(5L), eq("MARK_AS_READ"),
                    eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("should not re-save or re-audit when notification already read")
        void alreadyRead_noSave() {
            NotificationJob job = buildJob(5L, 42L);
            job.setSentAt(LocalDateTime.now().minusHours(1));

            when(jobRepository.findById(5L)).thenReturn(Optional.of(job));

            setUpAuthenticatedEmployee(42L);

            notificationService.markNotificationAsRead(5L);

            verify(jobRepository, never()).save(any());
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when notification not found")
        void notFound_throws() {
            when(jobRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.markNotificationAsRead(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("NotificationJob");
        }

        @Test
        @DisplayName("should throw BusinessException (BL 17.3) when caller is not the recipient")
        void ownershipViolation_throws() {
            NotificationJob job = buildJob(5L, 99L); // owned by employeeId=99
            when(jobRepository.findById(5L)).thenReturn(Optional.of(job));

            setUpAuthenticatedEmployee(42L); // authenticated as employeeId=42, not the owner

            assertThatThrownBy(() -> notificationService.markNotificationAsRead(5L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo("NOT_NOTIFICATION_RECIPIENT");

            verify(jobRepository, never()).save(any());
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("ADMIN should be able to mark any notification as read (BL 17.3 admin override)")
        void adminCanMarkAnyNotification() {
            NotificationJob job = buildJob(5L, 99L); // owned by employeeId=99
            job.setSentAt(null);

            when(jobRepository.findById(5L)).thenReturn(Optional.of(job));
            when(jobRepository.save(job)).thenReturn(job);

            setUpAuthenticatedAdmin(1L); // admin employee

            notificationService.markNotificationAsRead(5L);

            verify(jobRepository).save(job);
        }

        @Test
        @DisplayName("audit level must be REGULAR for mark-as-read")
        void auditLevelRegular() {
            NotificationJob job = buildJob(5L, 42L);
            job.setSentAt(null);

            when(jobRepository.findById(5L)).thenReturn(Optional.of(job));
            when(jobRepository.save(job)).thenReturn(job);

            setUpAuthenticatedEmployee(42L);

            notificationService.markNotificationAsRead(5L);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("unauthenticated call should not throw ownership error (server-side calls)")
        void unauthenticatedSkipsOwnershipCheck() {
            NotificationJob job = buildJob(5L, 99L);
            job.setSentAt(null);

            when(jobRepository.findById(5L)).thenReturn(Optional.of(job));
            when(jobRepository.save(job)).thenReturn(job);

            // No security context set — simulates internal/scheduler calls

            notificationService.markNotificationAsRead(5L);

            verify(jobRepository).save(job);
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("markAllNotificationsAsRead")
    class MarkAllNotificationsAsRead {

        @Test
        @DisplayName("should mark all pending/scheduled notifications as SENT and log REGULAR audit")
        void happyPath() {
            NotificationJob job1 = buildJob(1L, 10L);
            NotificationJob job2 = buildJob(2L, 10L);
            job1.setStatus(NotificationStatus.PENDING);
            job2.setStatus(NotificationStatus.SCHEDULED);

            org.springframework.data.domain.Page<NotificationJob> page =
                    new org.springframework.data.domain.PageImpl<>(List.of(job1, job2));
            when(jobRepository.findByRecipientAndChannelAndStatusIn(
                    eq(RecipientType.EMPLOYEE), eq(10L), eq(NotificationChannel.IN_APP),
                    any(), any())).thenReturn(page);
            when(jobRepository.saveAll(any())).thenReturn(List.of(job1, job2));

            notificationService.markAllNotificationsAsRead(RecipientType.EMPLOYEE, 10L, NotificationChannel.IN_APP);

            assertThat(job1.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(job2.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(job1.getSentAt()).isNotNull();
            verify(jobRepository).saveAll(List.of(job1, job2));
            verify(auditService).log(eq(AuditCategory.SYSTEM), eq(10L), eq("MARK_ALL_AS_READ"),
                    eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("should complete without error when no unread notifications exist")
        void noUnreadNotifications() {
            org.springframework.data.domain.Page<NotificationJob> emptyPage =
                    org.springframework.data.domain.Page.empty();
            when(jobRepository.findByRecipientAndChannelAndStatusIn(
                    any(), any(), any(), any(), any())).thenReturn(emptyPage);
            when(jobRepository.saveAll(any())).thenReturn(Collections.emptyList());

            notificationService.markAllNotificationsAsRead(RecipientType.EMPLOYEE, 99L, NotificationChannel.IN_APP);

            verify(auditService).log(any(), any(), any(), any(), any(), any());
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getNotificationById")
    class GetNotificationById {

        @Test
        @DisplayName("should return notification with rendered template details")
        void happyPath() {
            NotificationJob job = buildJob(5L, 10L);
            when(jobRepository.findById(5L)).thenReturn(Optional.of(job));
            when(templateRepository.findLatestByNameAndChannelAndLocale(any(), any(), any()))
                    .thenReturn(Optional.empty());

            NotificationJobResponse result = notificationService.getNotificationById(5L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when notification not found")
        void notFound_throws() {
            when(jobRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.getNotificationById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("NotificationJob");
        }

        @Test
        @DisplayName("should use template title and rendered message when template exists")
        void rendersTemplateWhenAvailable() {
            NotificationJob job = buildJob(5L, 10L);
            when(jobRepository.findById(5L)).thenReturn(Optional.of(job));

            NotificationTemplate template = NotificationTemplate.builder()
                    .id(1L).subject("Inquiry Assigned").content("Hello {{name}}")
                    .channel(NotificationChannel.IN_APP).build();
            when(templateRepository.findLatestByNameAndChannelAndLocale(any(), any(), any()))
                    .thenReturn(Optional.of(template));
            when(templateService.renderTemplate(eq("Hello {{name}}"), any())).thenReturn("Hello John");

            NotificationJobResponse result = notificationService.getNotificationById(5L);

            assertThat(result.getTitle()).isEqualTo("Inquiry Assigned");
            assertThat(result.getMessage()).isEqualTo("Hello John");
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getUnreadCount")
    class GetUnreadCount {

        @Test
        @DisplayName("should return count from repository")
        void happyPath() {
            when(jobRepository.countUnreadNotifications(RecipientType.EMPLOYEE, 10L, NotificationChannel.IN_APP))
                    .thenReturn(7L);

            Long count = notificationService.getUnreadCount(RecipientType.EMPLOYEE, 10L, NotificationChannel.IN_APP);

            assertThat(count).isEqualTo(7L);
        }

        @Test
        @DisplayName("should return 0 when repository returns null")
        void nullFromRepository_returnsZero() {
            when(jobRepository.countUnreadNotifications(any(), any(), any())).thenReturn(null);

            Long count = notificationService.getUnreadCount(RecipientType.EMPLOYEE, 10L, NotificationChannel.IN_APP);

            assertThat(count).isZero();
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("processScheduledNotifications")
    class ProcessScheduledNotifications {

        @Test
        @DisplayName("should set SCHEDULED jobs to PENDING and save them")
        void happyPath() {
            NotificationJob job1 = buildJob(1L, 10L);
            NotificationJob job2 = buildJob(2L, 10L);
            job1.setStatus(NotificationStatus.SCHEDULED);
            job2.setStatus(NotificationStatus.SCHEDULED);

            when(jobRepository.findByStatusAndScheduledForBefore(eq(NotificationStatus.SCHEDULED), any()))
                    .thenReturn(List.of(job1, job2));
            when(jobRepository.saveAll(any())).thenReturn(List.of(job1, job2));

            notificationService.processScheduledNotifications();

            assertThat(job1.getStatus()).isEqualTo(NotificationStatus.PENDING);
            assertThat(job2.getStatus()).isEqualTo(NotificationStatus.PENDING);
            verify(jobRepository).saveAll(List.of(job1, job2));
        }

        @Test
        @DisplayName("should complete without error when no scheduled jobs exist")
        void noScheduledJobs() {
            when(jobRepository.findByStatusAndScheduledForBefore(any(), any()))
                    .thenReturn(Collections.emptyList());
            when(jobRepository.saveAll(any())).thenReturn(Collections.emptyList());

            notificationService.processScheduledNotifications();

            verify(jobRepository).saveAll(Collections.emptyList());
        }
    }
}
