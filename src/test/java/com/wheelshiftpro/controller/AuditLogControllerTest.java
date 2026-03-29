package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.AuditLogResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.service.AuditService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogController Tests")
class AuditLogControllerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditLogController auditLogController;

    @Nested
    @DisplayName("getAuditLogs")
    class GetAuditLogs {

        @Test
        @DisplayName("should return audit logs successfully")
        void happyPath() {
            AuditLogResponse log = AuditLogResponse.builder()
                    .id(1L)
                    .category(AuditCategory.CAR)
                    .entityId(100L)
                    .action("CREATE")
                    .level(AuditLevel.REGULAR)
                    .performedById(10L)
                    .performedByName("John Doe")
                    .details("Created car")
                    .createdAt(LocalDateTime.now())
                    .build();

            PageResponse<AuditLogResponse> pageResponse = PageResponse.<AuditLogResponse>builder()
                    .content(List.of(log))
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(1L)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(auditService.getAuditLogs(null, null, null, null, null, 0, 20))
                    .thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> response =
                    auditLogController.getAuditLogs(null, null, null, null, null, 0, 20);

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getData()).isNotNull();
            assertThat(response.getBody().getData().getContent()).hasSize(1);
            assertThat(response.getBody().getData().getContent().get(0).getCategory()).isEqualTo(AuditCategory.CAR);
            assertThat(response.getBody().getData().getContent().get(0).getAction()).isEqualTo("CREATE");

            verify(auditService).getAuditLogs(null, null, null, null, null, 0, 20);
        }

        @Test
        @DisplayName("should apply category filter")
        void filterByCategory() {
            PageResponse<AuditLogResponse> pageResponse = PageResponse.<AuditLogResponse>builder()
                    .content(List.of())
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(0L)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(auditService.getAuditLogs(eq(AuditCategory.CAR), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                    .thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> response =
                    auditLogController.getAuditLogs(AuditCategory.CAR, null, null, null, null, 0, 20);

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            verify(auditService).getAuditLogs(eq(AuditCategory.CAR), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20));
        }

        @Test
        @DisplayName("should apply level filter")
        void filterByLevel() {
            PageResponse<AuditLogResponse> pageResponse = PageResponse.<AuditLogResponse>builder()
                    .content(List.of())
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(0L)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(auditService.getAuditLogs(isNull(), eq(AuditLevel.CRITICAL), isNull(), isNull(), isNull(), eq(0), eq(20)))
                    .thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> response =
                    auditLogController.getAuditLogs(null, AuditLevel.CRITICAL, null, null, null, 0, 20);

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            verify(auditService).getAuditLogs(isNull(), eq(AuditLevel.CRITICAL), isNull(), isNull(), isNull(), eq(0), eq(20));
        }

        @Test
        @DisplayName("should apply action filter")
        void filterByAction() {
            PageResponse<AuditLogResponse> pageResponse = PageResponse.<AuditLogResponse>builder()
                    .content(List.of())
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(0L)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(auditService.getAuditLogs(isNull(), isNull(), eq("DELETE"), isNull(), isNull(), eq(0), eq(20)))
                    .thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> response =
                    auditLogController.getAuditLogs(null, null, "DELETE", null, null, 0, 20);

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            verify(auditService).getAuditLogs(isNull(), isNull(), eq("DELETE"), isNull(), isNull(), eq(0), eq(20));
        }

        @Test
        @DisplayName("should apply entityId filter")
        void filterByEntityId() {
            PageResponse<AuditLogResponse> pageResponse = PageResponse.<AuditLogResponse>builder()
                    .content(List.of())
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(0L)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(auditService.getAuditLogs(isNull(), isNull(), isNull(), eq(100L), isNull(), eq(0), eq(20)))
                    .thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> response =
                    auditLogController.getAuditLogs(null, null, null, 100L, null, 0, 20);

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            verify(auditService).getAuditLogs(isNull(), isNull(), isNull(), eq(100L), isNull(), eq(0), eq(20));
        }

        @Test
        @DisplayName("should apply performedById filter")
        void filterByPerformedById() {
            PageResponse<AuditLogResponse> pageResponse = PageResponse.<AuditLogResponse>builder()
                    .content(List.of())
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(0L)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(auditService.getAuditLogs(isNull(), isNull(), isNull(), isNull(), eq(25L), eq(0), eq(20)))
                    .thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> response =
                    auditLogController.getAuditLogs(null, null, null, null, 25L, 0, 20);

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            verify(auditService).getAuditLogs(isNull(), isNull(), isNull(), isNull(), eq(25L), eq(0), eq(20));
        }

        @Test
        @DisplayName("should apply multiple filters")
        void multipleFilters() {
            PageResponse<AuditLogResponse> pageResponse = PageResponse.<AuditLogResponse>builder()
                    .content(List.of())
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(0L)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(auditService.getAuditLogs(
                    eq(AuditCategory.FINANCIAL_TRANSACTION),
                    eq(AuditLevel.CRITICAL),
                    eq("CREATE"),
                    eq(50L),
                    eq(10L),
                    eq(0),
                    eq(20)
            )).thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> response =
                    auditLogController.getAuditLogs(
                            AuditCategory.FINANCIAL_TRANSACTION,
                            AuditLevel.CRITICAL,
                            "CREATE",
                            50L,
                            10L,
                            0,
                            20
                    );

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            verify(auditService).getAuditLogs(
                    eq(AuditCategory.FINANCIAL_TRANSACTION),
                    eq(AuditLevel.CRITICAL),
                    eq("CREATE"),
                    eq(50L),
                    eq(10L),
                    eq(0),
                    eq(20)
            );
        }

        @Test
        @DisplayName("should apply custom pagination")
        void customPagination() {
            PageResponse<AuditLogResponse> pageResponse = PageResponse.<AuditLogResponse>builder()
                    .content(List.of())
                    .pageNumber(2)
                    .pageSize(50)
                    .totalElements(0L)
                    .totalPages(0)
                    .first(false)
                    .last(true)
                    .build();

            when(auditService.getAuditLogs(isNull(), isNull(), isNull(), isNull(), isNull(), eq(2), eq(50)))
                    .thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> response =
                    auditLogController.getAuditLogs(null, null, null, null, null, 2, 50);

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getPageNumber()).isEqualTo(2);
            assertThat(response.getBody().getData().getPageSize()).isEqualTo(50);

            verify(auditService).getAuditLogs(isNull(), isNull(), isNull(), isNull(), isNull(), eq(2), eq(50));
        }

        @Test
        @DisplayName("should return empty page when no results match")
        void noResults_emptyPage() {
            PageResponse<AuditLogResponse> pageResponse = PageResponse.<AuditLogResponse>builder()
                    .content(List.of())
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(0L)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(auditService.getAuditLogs(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> response =
                    auditLogController.getAuditLogs(AuditCategory.CAR, AuditLevel.CRITICAL, "NONEXISTENT", 9999L, 9999L, 0, 10);

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getContent()).isEmpty();
            assertThat(response.getBody().getData().getTotalElements()).isZero();
        }
    }
}
