package com.wheelshiftpro.service.impl.rbac;

import com.wheelshiftpro.dto.request.rbac.PermissionRequest;
import com.wheelshiftpro.dto.response.rbac.PermissionResponse;
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
import com.wheelshiftpro.repository.rbac.EmployeePermissionRepository;
import com.wheelshiftpro.repository.rbac.PermissionRepository;
import com.wheelshiftpro.service.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
@DisplayName("PermissionServiceImpl Tests")
class PermissionServiceImplTest {

    @Mock private PermissionRepository permissionRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeePermissionRepository employeePermissionRepository;
    @Mock private AuditService auditService;

    @InjectMocks private PermissionServiceImpl permissionService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Permission buildPermission(Long id, String resource, String action) {
        return Permission.builder()
                .id(id)
                .resource(resource)
                .action(action)
                .name(resource + ":" + action)
                .description("desc")
                .roles(new HashSet<>())
                .build();
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createPermission")
    class CreatePermission {

        @Test
        @DisplayName("should create permission and log CRITICAL audit")
        void happyPath() {
            PermissionRequest request = new PermissionRequest();
            request.setResource("cars");
            request.setAction("read");
            request.setDescription("Read cars");

            when(permissionRepository.existsByName("cars:read")).thenReturn(false);

            Permission saved = buildPermission(10L, "cars", "read");
            when(permissionRepository.save(any(Permission.class))).thenReturn(saved);

            PermissionResponse result = permissionService.createPermission(request);

            assertThat(result.getName()).isEqualTo("cars:read");
            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(10L),
                    eq("CREATE_PERMISSION"), eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when permission name already exists")
        void duplicateName_throws() {
            PermissionRequest request = new PermissionRequest();
            request.setResource("cars");
            request.setAction("read");

            when(permissionRepository.existsByName("cars:read")).thenReturn(true);

            assertThatThrownBy(() -> permissionService.createPermission(request))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(permissionRepository, never()).save(any());
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("audit level must be CRITICAL")
        void auditLevelCritical() {
            PermissionRequest request = new PermissionRequest();
            request.setResource("cars");
            request.setAction("write");

            when(permissionRepository.existsByName("cars:write")).thenReturn(false);
            when(permissionRepository.save(any())).thenReturn(buildPermission(1L, "cars", "write"));

            permissionService.createPermission(request);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.CRITICAL), any(), any());
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("updatePermission")
    class UpdatePermission {

        @Test
        @DisplayName("should update description and log CRITICAL audit")
        void happyPath() {
            Permission existing = buildPermission(5L, "cars", "read");
            when(permissionRepository.findById(5L)).thenReturn(Optional.of(existing));
            when(permissionRepository.save(existing)).thenReturn(existing);

            PermissionRequest request = new PermissionRequest();
            request.setResource("cars");
            request.setAction("read");
            request.setDescription("updated description");

            PermissionResponse result = permissionService.updatePermission(5L, request);

            assertThat(result).isNotNull();
            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(5L),
                    eq("UPDATE_PERMISSION"), eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when permission not found")
        void notFound_throws() {
            when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permissionService.updatePermission(99L, new PermissionRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Permission");
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deletePermission")
    class DeletePermission {

        @Test
        @DisplayName("should delete permission with no assigned roles and log CRITICAL audit")
        void happyPath() {
            Permission permission = buildPermission(5L, "cars", "read");
            when(permissionRepository.findById(5L)).thenReturn(Optional.of(permission));

            permissionService.deletePermission(5L);

            verify(permissionRepository).delete(permission);
            verify(auditService).log(eq(AuditCategory.EMPLOYEE), eq(5L),
                    eq("DELETE_PERMISSION"), eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("should throw BusinessException when permission is assigned to roles")
        void permissionHasRoles_throws() {
            Role role = Role.builder().id(1L).name(RoleType.SALES).isSystem(false)
                    .permissions(new HashSet<>()).employees(new HashSet<>()).build();
            Permission permission = buildPermission(5L, "cars", "read");
            permission.getRoles().add(role);

            when(permissionRepository.findById(5L)).thenReturn(Optional.of(permission));

            assertThatThrownBy(() -> permissionService.deletePermission(5L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo("PERMISSION_HAS_ROLES");

            verify(permissionRepository, never()).delete(any());
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when permission not found")
        void notFound_throws() {
            when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permissionService.deletePermission(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("audit level must be CRITICAL for delete")
        void auditLevelCritical() {
            Permission permission = buildPermission(5L, "cars", "read");
            when(permissionRepository.findById(5L)).thenReturn(Optional.of(permission));

            permissionService.deletePermission(5L);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.CRITICAL), any(), any());
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("hasPermission")
    class HasPermission {

        @Test
        @DisplayName("should return true when employee has permission through roles")
        void hasPermissionViaRole() {
            Employee emp = new Employee();
            emp.setId(1L);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));

            Permission perm = buildPermission(10L, "cars", "read");
            when(permissionRepository.findByEmployeeId(1L)).thenReturn(Set.of(perm));

            assertThat(permissionService.hasPermission(1L, "cars:read")).isTrue();
        }

        @Test
        @DisplayName("should return true when employee has custom permission")
        void hasCustomPermission() {
            Employee emp = new Employee();
            emp.setId(1L);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
            when(permissionRepository.findByEmployeeId(1L)).thenReturn(Collections.emptySet());
            when(employeePermissionRepository.findPermissionNamesByEmployeeId(1L))
                    .thenReturn(Set.of("transactions:read"));

            assertThat(permissionService.hasPermission(1L, "transactions:read")).isTrue();
        }

        @Test
        @DisplayName("should return false when employee has neither role nor custom permission")
        void noPermission() {
            Employee emp = new Employee();
            emp.setId(1L);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
            when(permissionRepository.findByEmployeeId(1L)).thenReturn(Collections.emptySet());
            when(employeePermissionRepository.findPermissionNamesByEmployeeId(1L))
                    .thenReturn(Collections.emptySet());

            assertThat(permissionService.hasPermission(1L, "cars:delete")).isFalse();
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when employee not found")
        void employeeNotFound_throws() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permissionService.hasPermission(99L, "cars:read"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee");
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getEmployeePermissions")
    class GetEmployeePermissions {

        @Test
        @DisplayName("should return union of role and custom permissions")
        void returnsUnionOfPermissions() {
            Employee emp = new Employee();
            emp.setId(1L);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));

            Permission rolePerm = buildPermission(10L, "cars", "read");
            when(permissionRepository.findByEmployeeId(1L)).thenReturn(Set.of(rolePerm));
            when(employeePermissionRepository.findPermissionNamesByEmployeeId(1L))
                    .thenReturn(Set.of("transactions:read"));

            Set<String> result = permissionService.getEmployeePermissions(1L);

            assertThat(result).containsExactlyInAnyOrder("cars:read", "transactions:read");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when employee not found")
        void employeeNotFound_throws() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permissionService.getEmployeePermissions(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
