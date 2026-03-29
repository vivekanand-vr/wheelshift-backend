package com.wheelshiftpro.service.impl.rbac;

import com.wheelshiftpro.dto.request.rbac.DataScopeRequest;
import com.wheelshiftpro.dto.response.rbac.DataScopeResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.rbac.EmployeeDataScope;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.notifications.NotificationEventType;
import com.wheelshiftpro.enums.rbac.ScopeEffect;
import com.wheelshiftpro.enums.rbac.ScopeType;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.rbac.EmployeeDataScopeRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
import com.wheelshiftpro.service.notifications.NotificationEventHelper;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataScopeServiceImpl Tests")
class DataScopeServiceImplTest {

    @Mock private EmployeeDataScopeRepository dataScopeRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private AuditService auditService;
    @Mock private NotificationEventHelper notificationEventHelper;

    @InjectMocks private DataScopeServiceImpl dataScopeService;

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

    private Employee buildEmployee(Long id) {
        Employee emp = new Employee();
        emp.setId(id);
        return emp;
    }

    private EmployeeDataScope buildScope(Long id, Employee employee, ScopeType type,
                                         String value, ScopeEffect effect) {
        return EmployeeDataScope.builder()
                .id(id)
                .employee(employee)
                .scopeType(type)
                .scopeValue(value)
                .effect(effect)
                .build();
    }

    private DataScopeRequest buildRequest(ScopeType type, String value, ScopeEffect effect) {
        DataScopeRequest request = new DataScopeRequest();
        request.setScopeType(type);
        request.setScopeValue(value);
        request.setEffect(effect);
        return request;
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("addScopeToEmployee")
    class AddScopeToEmployee {

        @Test
        @DisplayName("should add scope, dispatch notification, and log CRITICAL audit")
        void happyPath() {
            Employee employee = buildEmployee(1L);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(dataScopeRepository.findByEmployeeIdAndScopeTypeAndScopeValue(
                    1L, ScopeType.LOCATION, "LOC-01")).thenReturn(null);

            EmployeeDataScope saved = buildScope(10L, employee, ScopeType.LOCATION, "LOC-01", ScopeEffect.INCLUDE);
            when(dataScopeRepository.save(any(EmployeeDataScope.class))).thenReturn(saved);

            DataScopeRequest request = buildRequest(ScopeType.LOCATION, "LOC-01", ScopeEffect.INCLUDE);
            DataScopeResponse result = dataScopeService.addScopeToEmployee(1L, request);

            assertThat(result).isNotNull();
            assertThat(result.getScopeType()).isEqualTo(ScopeType.LOCATION);
            assertThat(result.getScopeValue()).isEqualTo("LOC-01");

            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(10L), eq("CREATE_DATA_SCOPE"),
                    eq(AuditLevel.CRITICAL), any(), any());
            verify(notificationEventHelper).notifyEmployee(eq(1L),
                    eq(NotificationEventType.DATA_SCOPE_CHANGED), any(), eq(10L), any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when employee not found")
        void employeeNotFound_throws() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dataScopeService.addScopeToEmployee(99L,
                    buildRequest(ScopeType.LOCATION, "LOC-01", ScopeEffect.INCLUDE)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee");

            verifyNoInteractions(auditService);
            verifyNoInteractions(notificationEventHelper);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when scope already exists")
        void duplicateScope_throws() {
            Employee employee = buildEmployee(1L);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

            EmployeeDataScope existing = buildScope(5L, employee, ScopeType.LOCATION, "LOC-01", ScopeEffect.INCLUDE);
            when(dataScopeRepository.findByEmployeeIdAndScopeTypeAndScopeValue(
                    1L, ScopeType.LOCATION, "LOC-01")).thenReturn(existing);

            assertThatThrownBy(() -> dataScopeService.addScopeToEmployee(1L,
                    buildRequest(ScopeType.LOCATION, "LOC-01", ScopeEffect.INCLUDE)))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(dataScopeRepository, never()).save(any());
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("audit level must be CRITICAL")
        void auditLevelCritical() {
            Employee employee = buildEmployee(1L);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(dataScopeRepository.findByEmployeeIdAndScopeTypeAndScopeValue(any(), any(), any())).thenReturn(null);
            when(dataScopeRepository.save(any())).thenReturn(buildScope(1L, employee, ScopeType.LOCATION, "X", ScopeEffect.INCLUDE));

            dataScopeService.addScopeToEmployee(1L, buildRequest(ScopeType.LOCATION, "X", ScopeEffect.INCLUDE));

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("audit performedBy is populated when authenticated")
        void auditFieldPopulatedWhenAuthenticated() {
            setUpAuthenticatedEmployee(42L);

            Employee employee = buildEmployee(1L);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(dataScopeRepository.findByEmployeeIdAndScopeTypeAndScopeValue(any(), any(), any())).thenReturn(null);
            when(dataScopeRepository.save(any())).thenReturn(buildScope(1L, employee, ScopeType.DEPARTMENT, "HR", ScopeEffect.INCLUDE));

            dataScopeService.addScopeToEmployee(1L, buildRequest(ScopeType.DEPARTMENT, "HR", ScopeEffect.INCLUDE));

            ArgumentCaptor<Employee> empCaptor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), empCaptor.capture(), any());
            assertThat(empCaptor.getValue()).isNotNull();
            assertThat(empCaptor.getValue().getId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("audit performedBy is null when unauthenticated")
        void auditFieldNullWhenUnauthenticated() {
            Employee employee = buildEmployee(1L);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(dataScopeRepository.findByEmployeeIdAndScopeTypeAndScopeValue(any(), any(), any())).thenReturn(null);
            when(dataScopeRepository.save(any())).thenReturn(buildScope(1L, employee, ScopeType.LOCATION, "X", ScopeEffect.INCLUDE));

            dataScopeService.addScopeToEmployee(1L, buildRequest(ScopeType.LOCATION, "X", ScopeEffect.INCLUDE));

            ArgumentCaptor<Employee> empCaptor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), empCaptor.capture(), any());
            assertThat(empCaptor.getValue()).isNull();
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("updateScope")
    class UpdateScope {

        @Test
        @DisplayName("should update scope effect and log CRITICAL audit")
        void happyPath() {
            Employee employee = buildEmployee(1L);
            EmployeeDataScope scope = buildScope(5L, employee, ScopeType.LOCATION, "LOC-01", ScopeEffect.INCLUDE);
            when(dataScopeRepository.findById(5L)).thenReturn(Optional.of(scope));
            when(dataScopeRepository.save(scope)).thenReturn(scope);

            DataScopeRequest request = buildRequest(ScopeType.LOCATION, "LOC-01", ScopeEffect.EXCLUDE);
            request.setDescription("updated");

            DataScopeResponse result = dataScopeService.updateScope(5L, request);

            assertThat(result).isNotNull();
            assertThat(scope.getEffect()).isEqualTo(ScopeEffect.EXCLUDE);
            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(5L), eq("UPDATE_DATA_SCOPE"),
                    eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when scope not found")
        void notFound_throws() {
            when(dataScopeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dataScopeService.updateScope(99L, buildRequest(ScopeType.LOCATION, "X", ScopeEffect.INCLUDE)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("EmployeeDataScope");
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("removeScopeFromEmployee")
    class RemoveScopeFromEmployee {

        @Test
        @DisplayName("should remove scope and log CRITICAL audit")
        void happyPath() {
            Employee employee = buildEmployee(1L);
            EmployeeDataScope scope = buildScope(5L, employee, ScopeType.LOCATION, "LOC-01", ScopeEffect.INCLUDE);
            when(dataScopeRepository.findById(5L)).thenReturn(Optional.of(scope));

            dataScopeService.removeScopeFromEmployee(5L);

            verify(dataScopeRepository).delete(scope);
            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(5L), eq("DELETE_DATA_SCOPE"),
                    eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when scope not found")
        void notFound_throws() {
            when(dataScopeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dataScopeService.removeScopeFromEmployee(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("audit level must be CRITICAL for delete")
        void auditLevelCritical() {
            Employee employee = buildEmployee(1L);
            EmployeeDataScope scope = buildScope(5L, employee, ScopeType.LOCATION, "LOC", ScopeEffect.INCLUDE);
            when(dataScopeRepository.findById(5L)).thenReturn(Optional.of(scope));

            dataScopeService.removeScopeFromEmployee(5L);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.CRITICAL), any(), any());
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("hasScope")
    class HasScope {

        @Test
        @DisplayName("should return true when INCLUDE scope exists")
        void hasScopeInclude() {
            Employee employee = buildEmployee(1L);
            EmployeeDataScope scope = buildScope(1L, employee, ScopeType.LOCATION, "LOC-01", ScopeEffect.INCLUDE);
            when(dataScopeRepository.findByEmployeeIdAndScopeTypeAndScopeValue(
                    1L, ScopeType.LOCATION, "LOC-01")).thenReturn(scope);

            assertThat(dataScopeService.hasScope(1L, ScopeType.LOCATION, "LOC-01")).isTrue();
        }

        @Test
        @DisplayName("should return false when EXCLUDE scope exists")
        void hasScopeExclude() {
            Employee employee = buildEmployee(1L);
            EmployeeDataScope scope = buildScope(1L, employee, ScopeType.LOCATION, "LOC-01", ScopeEffect.EXCLUDE);
            when(dataScopeRepository.findByEmployeeIdAndScopeTypeAndScopeValue(
                    1L, ScopeType.LOCATION, "LOC-01")).thenReturn(scope);

            assertThat(dataScopeService.hasScope(1L, ScopeType.LOCATION, "LOC-01")).isFalse();
        }

        @Test
        @DisplayName("should return false when no scope exists")
        void noScope() {
            when(dataScopeRepository.findByEmployeeIdAndScopeTypeAndScopeValue(
                    1L, ScopeType.LOCATION, "LOC-99")).thenReturn(null);

            assertThat(dataScopeService.hasScope(1L, ScopeType.LOCATION, "LOC-99")).isFalse();
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getLocationScopes")
    class GetLocationScopes {

        @Test
        @DisplayName("should return only INCLUDE location scope values")
        void returnsIncludedLocationScopeValues() {
            Employee employee = buildEmployee(1L);
            EmployeeDataScope includeScope = buildScope(1L, employee, ScopeType.LOCATION, "LOC-01", ScopeEffect.INCLUDE);
            EmployeeDataScope excludeScope = buildScope(2L, employee, ScopeType.LOCATION, "LOC-02", ScopeEffect.EXCLUDE);

            when(dataScopeRepository.findByEmployeeIdAndScopeType(1L, ScopeType.LOCATION))
                    .thenReturn(List.of(includeScope, excludeScope));

            Set<String> result = dataScopeService.getLocationScopes(1L);

            assertThat(result).containsExactly("LOC-01");
        }

        @Test
        @DisplayName("should return empty set when no location scopes exist")
        void emptyWhenNoScopes() {
            when(dataScopeRepository.findByEmployeeIdAndScopeType(1L, ScopeType.LOCATION))
                    .thenReturn(Collections.emptyList());

            assertThat(dataScopeService.getLocationScopes(1L)).isEmpty();
        }
    }
}
