package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.response.AuditLogResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.entity.AuditLog;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditServiceImpl Tests")
class AuditServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditServiceImpl auditService;

    @Nested
    @DisplayName("log")
    class Log {

        @Test
        @DisplayName("should create and save audit log entry")
        void happyPath() {
            Employee performer = new Employee();
            performer.setId(10L);
            performer.setName("John Doe");

            auditService.log(AuditCategory.CAR, 100L, "CREATE", AuditLevel.REGULAR, performer, "Test details");

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(captor.capture());

            AuditLog saved = captor.getValue();
            assertThat(saved.getCategory()).isEqualTo(AuditCategory.CAR);
            assertThat(saved.getEntityId()).isEqualTo(100L);
            assertThat(saved.getAction()).isEqualTo("CREATE");
            assertThat(saved.getLevel()).isEqualTo(AuditLevel.REGULAR);
            assertThat(saved.getPerformedBy()).isEqualTo(performer);
            assertThat(saved.getDetails()).isEqualTo("Test details");
        }

        @Test
        @DisplayName("should handle null performer (system actions)")
        void nullPerformer_allowed() {
            auditService.log(AuditCategory.SYSTEM, 1L, "SCHEDULED_JOB", AuditLevel.INFO, null, "Cron job executed");

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(captor.capture());

            AuditLog saved = captor.getValue();
            assertThat(saved.getPerformedBy()).isNull();
            assertThat(saved.getCategory()).isEqualTo(AuditCategory.SYSTEM);
        }

        @Test
        @DisplayName("should log CRITICAL level for security operations")
        void criticalLevel_saved() {
            Employee admin = new Employee();
            admin.setId(1L);

            auditService.log(AuditCategory.EMPLOYEE, 5L, "DELETE", AuditLevel.CRITICAL, admin, "Employee deleted");

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(captor.capture());

            assertThat(captor.getValue().getLevel()).isEqualTo(AuditLevel.CRITICAL);
        }

        @Test
        @DisplayName("should log HIGH level for impactful operations")
        void highLevel_saved() {
            auditService.log(AuditCategory.CAR, 10L, "STATUS_CHANGE", AuditLevel.HIGH, null, "From AVAILABLE to SOLD");

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(captor.capture());

            assertThat(captor.getValue().getLevel()).isEqualTo(AuditLevel.HIGH);
        }
    }

    @Nested
    @DisplayName("getAuditLogs")
    class GetAuditLogs {

        @Test
        @DisplayName("should return paginated audit logs with all filters applied")
        void happyPath_withFilters() {
            Employee performer = new Employee();
            performer.setId(10L);
            performer.setName("John Doe");

            AuditLog log1 = AuditLog.builder()
                    .id(1L)
                    .category(AuditCategory.CAR)
                    .entityId(100L)
                    .action("CREATE")
                    .level(AuditLevel.REGULAR)
                    .performedBy(performer)
                    .details("Created car")
                    .createdAt(LocalDateTime.now())
                    .build();

            List<AuditLog> logs = List.of(log1);
            Page<AuditLog> page = new PageImpl<>(logs);

            when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PageResponse<AuditLogResponse> result = auditService.getAuditLogs(
                    AuditCategory.CAR, AuditLevel.REGULAR, "CREATE", 100L, 10L, 0, 20);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCategory()).isEqualTo(AuditCategory.CAR);
            assertThat(result.getContent().get(0).getAction()).isEqualTo("CREATE");
            assertThat(result.getContent().get(0).getPerformedById()).isEqualTo(10L);
            assertThat(result.getContent().get(0).getPerformedByName()).isEqualTo("John Doe");

            verify(auditLogRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("should return audit logs without filters")
        void noFilters_returnsAll() {
            List<AuditLog> logs = List.of(
                    AuditLog.builder().id(1L).category(AuditCategory.CAR).entityId(1L)
                            .action("CREATE").level(AuditLevel.REGULAR).details("Test")
                            .createdAt(LocalDateTime.now()).build(),
                    AuditLog.builder().id(2L).category(AuditCategory.MOTORCYCLE).entityId(2L)
                            .action("UPDATE").level(AuditLevel.HIGH).details("Test2")
                            .createdAt(LocalDateTime.now()).build()
            );
            Page<AuditLog> page = new PageImpl<>(logs);

            when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PageResponse<AuditLogResponse> result = auditService.getAuditLogs(
                    null, null, null, null, null, 0, 20);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            verify(auditLogRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("should filter by category only")
        void filterByCategory() {
            AuditLog log = AuditLog.builder()
                    .id(1L)
                    .category(AuditCategory.FINANCIAL_TRANSACTION)
                    .entityId(50L)
                    .action("CREATE")
                    .level(AuditLevel.CRITICAL)
                    .details("Financial transaction")
                    .createdAt(LocalDateTime.now())
                    .build();

            Page<AuditLog> page = new PageImpl<>(List.of(log));
            when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PageResponse<AuditLogResponse> result = auditService.getAuditLogs(
                    AuditCategory.FINANCIAL_TRANSACTION, null, null, null, null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCategory()).isEqualTo(AuditCategory.FINANCIAL_TRANSACTION);
        }

        @Test
        @DisplayName("should filter by level only")
        void filterByLevel() {
            AuditLog log = AuditLog.builder()
                    .id(1L)
                    .category(AuditCategory.EMPLOYEE)
                    .entityId(5L)
                    .action("DELETE")
                    .level(AuditLevel.CRITICAL)
                    .details("Deleted employee")
                    .createdAt(LocalDateTime.now())
                    .build();

            Page<AuditLog> page = new PageImpl<>(List.of(log));
            when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PageResponse<AuditLogResponse> result = auditService.getAuditLogs(
                    null, AuditLevel.CRITICAL, null, null, null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getLevel()).isEqualTo(AuditLevel.CRITICAL);
        }

        @Test
        @DisplayName("should filter by action (case-insensitive)")
        void filterByAction() {
            AuditLog log = AuditLog.builder()
                    .id(1L)
                    .category(AuditCategory.CAR)
                    .entityId(10L)
                    .action("DELETE")
                    .level(AuditLevel.HIGH)
                    .details("Car deleted")
                    .createdAt(LocalDateTime.now())
                    .build();

            Page<AuditLog> page = new PageImpl<>(List.of(log));
            when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PageResponse<AuditLogResponse> result = auditService.getAuditLogs(
                    null, null, "delete", null, null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAction()).isEqualToIgnoringCase("DELETE");
        }

        @Test
        @DisplayName("should filter by entityId")
        void filterByEntityId() {
            AuditLog log = AuditLog.builder()
                    .id(1L)
                    .category(AuditCategory.CAR)
                    .entityId(100L)
                    .action("UPDATE")
                    .level(AuditLevel.REGULAR)
                    .details("Updated car")
                    .createdAt(LocalDateTime.now())
                    .build();

            Page<AuditLog> page = new PageImpl<>(List.of(log));
            when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PageResponse<AuditLogResponse> result = auditService.getAuditLogs(
                    null, null, null, 100L, null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEntityId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("should filter by performedById")
        void filterByPerformedById() {
            Employee performer = new Employee();
            performer.setId(25L);
            performer.setName("Test User");

            AuditLog log = AuditLog.builder()
                    .id(1L)
                    .category(AuditCategory.CLIENT)
                    .entityId(5L)
                    .action("CREATE")
                    .level(AuditLevel.REGULAR)
                    .performedBy(performer)
                    .details("Created client")
                    .createdAt(LocalDateTime.now())
                    .build();

            Page<AuditLog> page = new PageImpl<>(List.of(log));
            when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PageResponse<AuditLogResponse> result = auditService.getAuditLogs(
                    null, null, null, null, 25L, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getPerformedById()).isEqualTo(25L);
        }

        @Test
        @DisplayName("should return empty page when no results match filters")
        void noMatches_emptyPage() {
            Page<AuditLog> emptyPage = new PageImpl<>(List.of());
            when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

            PageResponse<AuditLogResponse> result = auditService.getAuditLogs(
                    AuditCategory.CAR, AuditLevel.CRITICAL, "NONEXISTENT", 9999L, 9999L, 0, 10);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("should handle null performedBy in response mapping")
        void nullPerformedBy_handled() {
            AuditLog log = AuditLog.builder()
                    .id(1L)
                    .category(AuditCategory.SYSTEM)
                    .entityId(1L)
                    .action("SCHEDULED_JOB")
                    .level(AuditLevel.INFO)
                    .performedBy(null)
                    .details("System operation")
                    .createdAt(LocalDateTime.now())
                    .build();

            Page<AuditLog> page = new PageImpl<>(List.of(log));
            when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PageResponse<AuditLogResponse> result = auditService.getAuditLogs(
                    null, null, null, null, null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getPerformedById()).isNull();
            assertThat(result.getContent().get(0).getPerformedByName()).isNull();
        }
    }
}
