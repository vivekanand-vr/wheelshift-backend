package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.EmployeeRequest;
import com.wheelshiftpro.dto.response.EmployeeResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Sale;
import com.wheelshiftpro.entity.Task;
import com.wheelshiftpro.entity.rbac.Role;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.EmployeeStatus;
import com.wheelshiftpro.enums.rbac.RoleType;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.EmployeeMapper;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.SaleRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock EmployeeRepository employeeRepository;
    @Mock SaleRepository saleRepository;
    @Mock EmployeeMapper employeeMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuditService auditService;

    @InjectMocks
    EmployeeServiceImpl employeeService;

    private void setUpAuthenticatedEmployee(Long employeeId) {
        Employee e = new Employee();
        e.setId(employeeId);
        EmployeeUserDetails principal = mock(EmployeeUserDetails.class);
        when(principal.getId()).thenReturn(employeeId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
        when(employeeRepository.getReferenceById(employeeId)).thenReturn(e);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createEmployee
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createEmployee")
    class CreateEmployee {

        @Test
        void happyPath_savesHashesPasswordAndAudits() {
            EmployeeRequest request = requestWithPassword("john@example.com", "secret");
            Employee entity = employeeWithId(1L, "john@example.com");

            when(employeeRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(employeeMapper.toEntity(request)).thenReturn(entity);
            when(passwordEncoder.encode("secret")).thenReturn("hashed");
            when(employeeRepository.save(entity)).thenReturn(entity);
            when(employeeMapper.toResponse(entity)).thenReturn(new EmployeeResponse());

            employeeService.createEmployee(request);

            verify(passwordEncoder).encode("secret");
            assertThat(entity.getPasswordHash()).isEqualTo("hashed");
            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(1L), eq("CREATE"),
                    eq(AuditLevel.CRITICAL), isNull(), anyString());
        }

        @Test
        void duplicateEmail_throwsDuplicateResourceException() {
            EmployeeRequest request = requestWithPassword("taken@example.com", "secret");
            when(employeeRepository.existsByEmail("taken@example.com")).thenReturn(true);

            assertThatThrownBy(() -> employeeService.createEmployee(request))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(auditService, never()).log(any(), any(), any(), any(), any(), any());
        }

        @Test
        void nullPassword_doesNotHashOrSetPasswordHash() {
            EmployeeRequest request = requestWithPassword("a@a.com", null);
            Employee entity = employeeWithId(1L, "a@a.com");

            when(employeeRepository.existsByEmail("a@a.com")).thenReturn(false);
            when(employeeMapper.toEntity(request)).thenReturn(entity);
            when(employeeRepository.save(entity)).thenReturn(entity);
            when(employeeMapper.toResponse(entity)).thenReturn(new EmployeeResponse());

            employeeService.createEmployee(request);

            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        void auditField_populatedWhenAuthenticated() {
            setUpAuthenticatedEmployee(5L);

            EmployeeRequest request = requestWithPassword("b@b.com", "pass");
            Employee entity = employeeWithId(2L, "b@b.com");

            when(employeeRepository.existsByEmail("b@b.com")).thenReturn(false);
            when(employeeMapper.toEntity(request)).thenReturn(entity);
            when(passwordEncoder.encode("pass")).thenReturn("hashed2");
            when(employeeRepository.save(entity)).thenReturn(entity);
            when(employeeMapper.toResponse(entity)).thenReturn(new EmployeeResponse());

            employeeService.createEmployee(request);

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), captor.capture(), any());
            assertThat(captor.getValue()).isNotNull();
            assertThat(captor.getValue().getId()).isEqualTo(5L);
        }

        @Test
        void auditLevel_isCritical() {
            EmployeeRequest request = requestWithPassword("c@c.com", "pass");
            Employee entity = employeeWithId(3L, "c@c.com");

            when(employeeRepository.existsByEmail("c@c.com")).thenReturn(false);
            when(employeeMapper.toEntity(request)).thenReturn(entity);
            when(passwordEncoder.encode("pass")).thenReturn("h");
            when(employeeRepository.save(entity)).thenReturn(entity);
            when(employeeMapper.toResponse(entity)).thenReturn(new EmployeeResponse());

            employeeService.createEmployee(request);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.CRITICAL), any(), any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateEmployee
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateEmployee")
    class UpdateEmployee {

        @Test
        void happyPath_updatesAndAudits() {
            EmployeeRequest request = requestWithPassword("new@example.com", null);
            Employee entity = employeeWithId(1L, "old@example.com");

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(employeeRepository.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(false);
            when(employeeRepository.save(entity)).thenReturn(entity);
            when(employeeMapper.toResponse(entity)).thenReturn(new EmployeeResponse());

            employeeService.updateEmployee(1L, request);

            verify(employeeMapper).updateEntityFromRequest(request, entity);
            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(1L), eq("UPDATE"),
                    eq(AuditLevel.CRITICAL), isNull(), anyString());
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.updateEmployee(99L, requestWithPassword("x@x.com", null)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void duplicateEmailOnUpdate_throwsDuplicateResourceException() {
            EmployeeRequest request = requestWithPassword("taken@example.com", null);
            Employee entity = employeeWithId(1L, "old@example.com");

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(employeeRepository.existsByEmailAndIdNot("taken@example.com", 1L)).thenReturn(true);

            assertThatThrownBy(() -> employeeService.updateEmployee(1L, request))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        void passwordProvided_hashesAndSetsPasswordHash() {
            EmployeeRequest request = requestWithPassword("a@a.com", "newPass");
            Employee entity = employeeWithId(1L, "a@a.com");

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(employeeRepository.existsByEmailAndIdNot("a@a.com", 1L)).thenReturn(false);
            when(passwordEncoder.encode("newPass")).thenReturn("newHashed");
            when(employeeRepository.save(entity)).thenReturn(entity);
            when(employeeMapper.toResponse(entity)).thenReturn(new EmployeeResponse());

            employeeService.updateEmployee(1L, request);

            verify(passwordEncoder).encode("newPass");
            assertThat(entity.getPasswordHash()).isEqualTo("newHashed");
        }

        @Test
        void auditLevel_isCritical() {
            EmployeeRequest request = requestWithPassword("a@a.com", null);
            Employee entity = employeeWithId(1L, "a@a.com");

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(employeeRepository.existsByEmailAndIdNot("a@a.com", 1L)).thenReturn(false);
            when(employeeRepository.save(entity)).thenReturn(entity);
            when(employeeMapper.toResponse(entity)).thenReturn(new EmployeeResponse());

            employeeService.updateEmployee(1L, request);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.CRITICAL), any(), any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteEmployee
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteEmployee")
    class DeleteEmployee {

        @Test
        void happyPath_deletesAndAudits() {
            Employee entity = employeeWithId(1L, "a@a.com");

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));

            employeeService.deleteEmployee(1L);

            verify(employeeRepository).delete(entity);
            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(1L), eq("DELETE"),
                    eq(AuditLevel.CRITICAL), isNull(), anyString());
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void hasSales_throwsBusinessException() {
            Employee entity = employeeWithId(1L, "a@a.com");
            entity.getHandledSales().add(new Sale());

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> employeeService.deleteEmployee(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("sales");
        }

        @Test
        void hasTasks_throwsBusinessException() {
            Employee entity = employeeWithId(1L, "a@a.com");
            entity.getAssignedTasks().add(new Task());

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> employeeService.deleteEmployee(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("tasks");
        }

        @Test
        void lastSuperAdmin_throwsBusinessException() {
            Employee entity = employeeWithSuperAdminRole(1L);

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(employeeRepository.countByRolesName(RoleType.SUPER_ADMIN)).thenReturn(1L);

            assertThatThrownBy(() -> employeeService.deleteEmployee(1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo("LAST_SUPER_ADMIN");
        }

        @Test
        void notLastSuperAdmin_deletesSuccessfully() {
            Employee entity = employeeWithSuperAdminRole(1L);

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(employeeRepository.countByRolesName(RoleType.SUPER_ADMIN)).thenReturn(2L);

            employeeService.deleteEmployee(1L);

            verify(employeeRepository).delete(entity);
        }

        @Test
        void auditLevel_isCritical() {
            Employee entity = employeeWithId(1L, "a@a.com");
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));

            employeeService.deleteEmployee(1L);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.CRITICAL), any(), any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateEmployeeStatus
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateEmployeeStatus")
    class UpdateEmployeeStatus {

        @Test
        void happyPath_updatesStatusAndAudits() {
            Employee entity = employeeWithId(1L, "a@a.com");
            entity.setStatus(EmployeeStatus.ACTIVE);

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(employeeRepository.save(entity)).thenReturn(entity);
            when(employeeMapper.toResponse(entity)).thenReturn(new EmployeeResponse());

            employeeService.updateEmployeeStatus(1L, EmployeeStatus.INACTIVE);

            assertThat(entity.getStatus()).isEqualTo(EmployeeStatus.INACTIVE);
            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(1L), eq("STATUS_CHANGE"),
                    eq(AuditLevel.CRITICAL), isNull(), contains("ACTIVE"));
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.updateEmployeeStatus(99L, EmployeeStatus.INACTIVE))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void selfSetInactive_throwsBusinessException() {
            setUpSecurityContextWithId(1L);
            Employee entity = employeeWithId(1L, "self@a.com");
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> employeeService.updateEmployeeStatus(1L, EmployeeStatus.INACTIVE))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo("SELF_STATUS_CHANGE_FORBIDDEN");
        }

        @Test
        void selfSetSuspended_throwsBusinessException() {
            setUpSecurityContextWithId(1L);
            Employee entity = employeeWithId(1L, "self@a.com");
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> employeeService.updateEmployeeStatus(1L, EmployeeStatus.SUSPENDED))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo("SELF_STATUS_CHANGE_FORBIDDEN");
        }

        @Test
        void selfSetActive_isAllowed() {
            setUpSecurityContextWithId(2L);
            Employee entity = employeeWithId(2L, "self@a.com");
            entity.setStatus(EmployeeStatus.INACTIVE);

            when(employeeRepository.findById(2L)).thenReturn(Optional.of(entity));
            when(employeeRepository.save(entity)).thenReturn(entity);
            when(employeeMapper.toResponse(entity)).thenReturn(new EmployeeResponse());

            employeeService.updateEmployeeStatus(2L, EmployeeStatus.ACTIVE);

            assertThat(entity.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        }

        @Test
        void otherEmployeeSetInactive_isAllowed() {
            setUpSecurityContextWithId(99L);
            Employee entity = employeeWithId(1L, "other@a.com");
            entity.setStatus(EmployeeStatus.ACTIVE);

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(employeeRepository.save(entity)).thenReturn(entity);
            when(employeeMapper.toResponse(entity)).thenReturn(new EmployeeResponse());

            employeeService.updateEmployeeStatus(1L, EmployeeStatus.INACTIVE);

            assertThat(entity.getStatus()).isEqualTo(EmployeeStatus.INACTIVE);
        }

        @Test
        void auditLevel_isCritical() {
            Employee entity = employeeWithId(1L, "a@a.com");
            entity.setStatus(EmployeeStatus.ACTIVE);

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(employeeRepository.save(entity)).thenReturn(entity);
            when(employeeMapper.toResponse(entity)).thenReturn(new EmployeeResponse());

            employeeService.updateEmployeeStatus(1L, EmployeeStatus.INACTIVE);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.CRITICAL), any(), any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getEmployeeById
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getEmployeeById")
    class GetEmployeeById {

        @Test
        void happyPath_returnsResponse() {
            Employee entity = employeeWithId(1L, "a@a.com");
            EmployeeResponse response = new EmployeeResponse();
            response.setId(1L);

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(employeeMapper.toResponse(entity)).thenReturn(response);

            assertThat(employeeService.getEmployeeById(1L).getId()).isEqualTo(1L);
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private EmployeeRequest requestWithPassword(String email, String password) {
        EmployeeRequest r = new EmployeeRequest();
        r.setEmail(email);
        r.setPassword(password);
        return r;
    }

    private Employee employeeWithId(Long id, String email) {
        Employee e = new Employee();
        e.setId(id);
        e.setEmail(email);
        e.setHandledSales(new ArrayList<>());
        e.setAssignedTasks(new ArrayList<>());
        e.setRoles(new HashSet<>());
        return e;
    }

    private Employee employeeWithSuperAdminRole(Long id) {
        Employee e = employeeWithId(id, "sa@a.com");
        Role role = new Role();
        role.setName(RoleType.SUPER_ADMIN);
        e.getRoles().add(role);
        return e;
    }

    private void setUpSecurityContextWithId(Long id) {
        EmployeeUserDetails principal = mock(EmployeeUserDetails.class);
        when(principal.getId()).thenReturn(id);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
    }
}
