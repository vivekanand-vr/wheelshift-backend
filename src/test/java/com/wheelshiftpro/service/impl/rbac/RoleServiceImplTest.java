package com.wheelshiftpro.service.impl.rbac;

import com.wheelshiftpro.dto.request.rbac.RoleRequest;
import com.wheelshiftpro.dto.response.rbac.RoleResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.rbac.Permission;
import com.wheelshiftpro.entity.rbac.Role;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.rbac.RoleType;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.rbac.PermissionRepository;
import com.wheelshiftpro.repository.rbac.RoleRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
import com.wheelshiftpro.service.CacheInvalidationService;
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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleServiceImpl Tests")
class RoleServiceImplTest {

    @Mock private RoleRepository roleRepository;
    @Mock private PermissionRepository permissionRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private AuditService auditService;
    @Mock private CacheInvalidationService cacheInvalidationService;

    @InjectMocks private RoleServiceImpl roleService;

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

    private Role buildCustomRole(Long id, RoleType name) {
        return Role.builder()
                .id(id)
                .name(name)
                .description("desc")
                .isSystem(false)
                .permissions(new HashSet<>())
                .employees(new HashSet<>())
                .build();
    }

    private Role buildSystemRole(Long id, RoleType name) {
        return Role.builder()
                .id(id)
                .name(name)
                .description("desc")
                .isSystem(true)
                .permissions(new HashSet<>())
                .employees(new HashSet<>())
                .build();
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createRole")
    class CreateRole {

        @Test
        @DisplayName("should create a custom role and log CRITICAL audit")
        void happyPath() {
            RoleRequest request = new RoleRequest();
            request.setName(RoleType.SALES);
            request.setDescription("Sales team role");

            when(roleRepository.existsByName(RoleType.SALES)).thenReturn(false);

            Role saved = buildCustomRole(10L, RoleType.SALES);
            when(roleRepository.save(any(Role.class))).thenReturn(saved);

            RoleResponse result = roleService.createRole(request);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(RoleType.SALES);
            assertThat(result.getIsSystem()).isFalse();

            ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
            verify(roleRepository).save(captor.capture());
            assertThat(captor.getValue().getIsSystem()).isFalse();

            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(10L), eq("CREATE_ROLE"),
                    eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when role name already exists")
        void duplicateName_throws() {
            RoleRequest request = new RoleRequest();
            request.setName(RoleType.ADMIN);

            when(roleRepository.existsByName(RoleType.ADMIN)).thenReturn(true);

            assertThatThrownBy(() -> roleService.createRole(request))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(roleRepository, never()).save(any());
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("audit level must be CRITICAL for role creation")
        void auditLevelCritical() {
            RoleRequest request = new RoleRequest();
            request.setName(RoleType.SALES);

            when(roleRepository.existsByName(RoleType.SALES)).thenReturn(false);
            when(roleRepository.save(any())).thenReturn(buildCustomRole(1L, RoleType.SALES));

            roleService.createRole(request);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("audit performedBy is populated when authenticated")
        void auditFieldPopulatedWhenAuthenticated() {
            setUpAuthenticatedEmployee(99L);

            RoleRequest request = new RoleRequest();
            request.setName(RoleType.SALES);

            when(roleRepository.existsByName(RoleType.SALES)).thenReturn(false);
            when(roleRepository.save(any())).thenReturn(buildCustomRole(1L, RoleType.SALES));

            roleService.createRole(request);

            ArgumentCaptor<Employee> empCaptor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), empCaptor.capture(), any());
            assertThat(empCaptor.getValue()).isNotNull();
            assertThat(empCaptor.getValue().getId()).isEqualTo(99L);
        }

        @Test
        @DisplayName("audit performedBy is null when unauthenticated")
        void auditFieldNullWhenUnauthenticated() {
            RoleRequest request = new RoleRequest();
            request.setName(RoleType.SALES);

            when(roleRepository.existsByName(RoleType.SALES)).thenReturn(false);
            when(roleRepository.save(any())).thenReturn(buildCustomRole(1L, RoleType.SALES));

            roleService.createRole(request);

            ArgumentCaptor<Employee> empCaptor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), empCaptor.capture(), any());
            assertThat(empCaptor.getValue()).isNull();
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("updateRole")
    class UpdateRole {

        @Test
        @DisplayName("should update description of a custom role")
        void happyPath() {
            Role existing = buildCustomRole(5L, RoleType.SALES);
            when(roleRepository.findById(5L)).thenReturn(Optional.of(existing));
            when(roleRepository.save(existing)).thenReturn(existing);

            RoleRequest request = new RoleRequest();
            request.setName(RoleType.SALES);
            request.setDescription("updated description");

            RoleResponse result = roleService.updateRole(5L, request);

            assertThat(result).isNotNull();
            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(5L), eq("UPDATE_ROLE"),
                    eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("should throw BusinessException when trying to update a system role")
        void systemRole_throws() {
            Role sysRole = buildSystemRole(1L, RoleType.SUPER_ADMIN);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(sysRole));

            assertThatThrownBy(() -> roleService.updateRole(1L, new RoleRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("system role")
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo("SYSTEM_ROLE_MODIFY");

            verify(roleRepository, never()).save(any());
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when role not found")
        void notFound_throws() {
            when(roleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roleService.updateRole(99L, new RoleRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deleteRole")
    class DeleteRole {

        @Test
        @DisplayName("should delete a custom role with no assigned employees")
        void happyPath() {
            Role role = buildCustomRole(5L, RoleType.SALES);
            when(roleRepository.findById(5L)).thenReturn(Optional.of(role));

            roleService.deleteRole(5L);

            verify(roleRepository).delete(role);
            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(5L), eq("DELETE_ROLE"),
                    eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("should throw BusinessException when deleting a system role")
        void systemRole_throws() {
            Role sysRole = buildSystemRole(1L, RoleType.ADMIN);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(sysRole));

            assertThatThrownBy(() -> roleService.deleteRole(1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo("SYSTEM_ROLE_DELETE");

            verify(roleRepository, never()).delete(any());
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("should throw BusinessException when role is assigned to employees")
        void roleHasEmployees_throws() {
            Employee emp = new Employee();
            emp.setId(1L);
            Role role = Role.builder()
                    .id(5L).name(RoleType.SALES).isSystem(false)
                    .employees(Set.of(emp)).permissions(new HashSet<>())
                    .build();
            when(roleRepository.findById(5L)).thenReturn(Optional.of(role));

            assertThatThrownBy(() -> roleService.deleteRole(5L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo("ROLE_HAS_EMPLOYEES");

            verify(roleRepository, never()).delete(any());
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when role not found")
        void notFound_throws() {
            when(roleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roleService.deleteRole(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("audit level must be CRITICAL for delete")
        void auditLevelCritical() {
            Role role = buildCustomRole(5L, RoleType.SALES);
            when(roleRepository.findById(5L)).thenReturn(Optional.of(role));

            roleService.deleteRole(5L);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.CRITICAL), any(), any());
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("assignRoleToEmployee")
    class AssignRoleToEmployee {

        @Test
        @DisplayName("should assign role to employee and log CRITICAL audit")
        void happyPath() {
            Employee employee = new Employee();
            employee.setId(1L);
            employee.setRoles(new HashSet<>());

            Role role = buildCustomRole(5L, RoleType.SALES);

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(roleRepository.findById(5L)).thenReturn(Optional.of(role));
            when(employeeRepository.save(employee)).thenReturn(employee);

            roleService.assignRoleToEmployee(1L, 5L);

            verify(employeeRepository).save(employee);
            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(1L), eq("ASSIGN_ROLE"),
                    eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when employee not found")
        void employeeNotFound_throws() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roleService.assignRoleToEmployee(99L, 5L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when role not found")
        void roleNotFound_throws() {
            Employee employee = new Employee();
            employee.setId(1L);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(roleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roleService.assignRoleToEmployee(1L, 99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Role");
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("removeRoleFromEmployee")
    class RemoveRoleFromEmployee {

        @Test
        @DisplayName("should remove role from employee and log CRITICAL audit")
        void happyPath() {
            Role role = buildCustomRole(5L, RoleType.SALES);
            Employee employee = new Employee();
            employee.setId(1L);
            employee.setRoles(new HashSet<>(Set.of(role)));

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(roleRepository.findById(5L)).thenReturn(Optional.of(role));
            when(employeeRepository.save(employee)).thenReturn(employee);

            roleService.removeRoleFromEmployee(1L, 5L);

            verify(employeeRepository).save(employee);
            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(1L), eq("REMOVE_ROLE"),
                    eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when employee not found")
        void employeeNotFound_throws() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roleService.removeRoleFromEmployee(99L, 5L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("addPermissionToRole")
    class AddPermissionToRole {

        @Test
        @DisplayName("should add permission to role, evict cache, and log CRITICAL audit")
        void happyPath() {
            Employee emp = new Employee();
            emp.setId(7L);
            Role role = Role.builder()
                    .id(5L).name(RoleType.SALES).isSystem(false)
                    .permissions(new HashSet<>())
                    .employees(new HashSet<>(Set.of(emp)))
                    .build();
            Permission permission = Permission.builder()
                    .id(10L).name("cars:read").resource("cars").action("read")
                    .roles(new HashSet<>()).build();

            when(roleRepository.findById(5L)).thenReturn(Optional.of(role));
            when(permissionRepository.findById(10L)).thenReturn(Optional.of(permission));
            when(roleRepository.save(role)).thenReturn(role);

            roleService.addPermissionToRole(5L, 10L);

            verify(roleRepository).save(role);
            verify(cacheInvalidationService).evictCache("employeePermissions", 7L);
            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(5L),
                    eq("ADD_PERMISSION_TO_ROLE"), eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when permission not found")
        void permissionNotFound_throws() {
            Role role = buildCustomRole(5L, RoleType.SALES);
            when(roleRepository.findById(5L)).thenReturn(Optional.of(role));
            when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roleService.addPermissionToRole(5L, 99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Permission");
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("removePermissionFromRole")
    class RemovePermissionFromRole {

        @Test
        @DisplayName("should remove permission from role, evict cache, and log CRITICAL audit")
        void happyPath() {
            Employee emp = new Employee();
            emp.setId(7L);
            Permission permission = Permission.builder()
                    .id(10L).name("cars:read").resource("cars").action("read")
                    .roles(new HashSet<>()).build();
            Role role = Role.builder()
                    .id(5L).name(RoleType.SALES).isSystem(false)
                    .permissions(new HashSet<>(Set.of(permission)))
                    .employees(new HashSet<>(Set.of(emp)))
                    .build();
            permission.getRoles().add(role);

            when(roleRepository.findById(5L)).thenReturn(Optional.of(role));
            when(permissionRepository.findById(10L)).thenReturn(Optional.of(permission));
            when(roleRepository.save(role)).thenReturn(role);

            roleService.removePermissionFromRole(5L, 10L);

            verify(roleRepository).save(role);
            verify(cacheInvalidationService).evictCache("employeePermissions", 7L);
            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(5L),
                    eq("REMOVE_PERMISSION_FROM_ROLE"), eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when role not found")
        void roleNotFound_throws() {
            when(roleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roleService.removePermissionFromRole(99L, 10L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when permission not found")
        void permissionNotFound_throws() {
            Role role = buildCustomRole(5L, RoleType.SALES);
            when(roleRepository.findById(5L)).thenReturn(Optional.of(role));
            when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roleService.removePermissionFromRole(5L, 99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
