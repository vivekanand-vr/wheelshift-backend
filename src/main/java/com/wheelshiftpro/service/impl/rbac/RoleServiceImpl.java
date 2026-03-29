package com.wheelshiftpro.service.impl.rbac;

import com.wheelshiftpro.dto.request.rbac.RoleRequest;
import com.wheelshiftpro.dto.response.rbac.PermissionResponse;
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
import com.wheelshiftpro.service.rbac.RoleService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation for Role operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;
    private final CacheInvalidationService cacheInvalidationService;

    @Override
    public RoleResponse createRole(RoleRequest request) {
        log.info("Creating new role: {}", request.getName());

        if (roleRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Role", "name", request.getName());
        }

        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isSystem(false)
                .build();

        role = roleRepository.save(role);
        log.info("Role created successfully with ID: {}", role.getId());

        auditService.log(AuditCategory.EMPLOYEE, role.getId(), "CREATE_ROLE",
                AuditLevel.CRITICAL, resolveCurrentEmployee(), "Role: " + role.getName());

        return mapToResponse(role);
    }

    @Override
    public RoleResponse updateRole(Long roleId, RoleRequest request) {
        log.info("Updating role with ID: {}", roleId);

        Role role = getRoleEntityById(roleId);

        if (role.getIsSystem()) {
            throw new BusinessException("Cannot modify a system role", "SYSTEM_ROLE_MODIFY");
        }

        role.setDescription(request.getDescription());
        role = roleRepository.save(role);

        log.info("Role updated successfully: {}", roleId);

        auditService.log(AuditCategory.EMPLOYEE, roleId, "UPDATE_ROLE",
                AuditLevel.CRITICAL, resolveCurrentEmployee(), "Role: " + role.getName());

        return mapToResponse(role);
    }

    @Override
    public void deleteRole(Long roleId) {
        log.info("Deleting role with ID: {}", roleId);

        Role role = getRoleEntityById(roleId);

        if (role.getIsSystem()) {
            throw new BusinessException("Cannot delete a system role", "SYSTEM_ROLE_DELETE");
        }

        if (!role.getEmployees().isEmpty()) {
            throw new BusinessException(
                    "Cannot delete role " + role.getName() + " because it is assigned to employees",
                    "ROLE_HAS_EMPLOYEES");
        }

        String roleName = role.getName().name();
        roleRepository.delete(role);
        log.info("Role deleted successfully: {}", roleId);

        auditService.log(AuditCategory.EMPLOYEE, roleId, "DELETE_ROLE",
                AuditLevel.CRITICAL, resolveCurrentEmployee(), "Role: " + roleName);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long roleId) {
        log.debug("Fetching role with ID: {}", roleId);
        return mapToResponse(getRoleEntityById(roleId));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleByName(RoleType name) {
        log.debug("Fetching role with name: {}", name);
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", name));
        return mapToResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        log.debug("Fetching all roles");
        return roleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<RoleResponse> getRolesByEmployeeId(Long employeeId) {
        log.debug("Fetching roles for employee ID: {}", employeeId);
        return roleRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toSet());
    }

    @Override
    public void assignRoleToEmployee(Long employeeId, Long roleId) {
        log.info("Assigning role {} to employee {}", roleId, employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        Role role = getRoleEntityById(roleId);

        employee.addRole(role);
        employeeRepository.save(employee);

        log.info("Role assigned successfully");

        auditService.log(AuditCategory.EMPLOYEE, employeeId, "ASSIGN_ROLE",
                AuditLevel.CRITICAL, resolveCurrentEmployee(),
                "Role " + role.getName() + " assigned to employee " + employeeId);
    }

    @Override
    public void removeRoleFromEmployee(Long employeeId, Long roleId) {
        log.info("Removing role {} from employee {}", roleId, employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        Role role = getRoleEntityById(roleId);

        employee.removeRole(role);
        employeeRepository.save(employee);

        log.info("Role removed successfully");

        auditService.log(AuditCategory.EMPLOYEE, employeeId, "REMOVE_ROLE",
                AuditLevel.CRITICAL, resolveCurrentEmployee(),
                "Role " + role.getName() + " removed from employee " + employeeId);
    }

    @Override
    public void addPermissionToRole(Long roleId, Long permissionId) {
        log.info("Adding permission {} to role {}", permissionId, roleId);

        Role role = getRoleEntityById(roleId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

        role.addPermission(permission);
        roleRepository.save(role);

        log.info("Permission added successfully");

        role.getEmployees().forEach(emp ->
                cacheInvalidationService.evictCache("employeePermissions", emp.getId()));

        auditService.log(AuditCategory.EMPLOYEE, roleId, "ADD_PERMISSION_TO_ROLE",
                AuditLevel.CRITICAL, resolveCurrentEmployee(),
                "Permission " + permission.getName() + " added to role " + role.getName());
    }

    @Override
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        log.info("Removing permission {} from role {}", permissionId, roleId);

        Role role = getRoleEntityById(roleId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

        role.removePermission(permission);
        roleRepository.save(role);

        log.info("Permission removed successfully");

        role.getEmployees().forEach(emp ->
                cacheInvalidationService.evictCache("employeePermissions", emp.getId()));

        auditService.log(AuditCategory.EMPLOYEE, roleId, "REMOVE_PERMISSION_FROM_ROLE",
                AuditLevel.CRITICAL, resolveCurrentEmployee(),
                "Permission " + permission.getName() + " removed from role " + role.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleEntityById(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleEntityByName(RoleType name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", name));
    }

    private Employee resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EmployeeUserDetails u) {
            return employeeRepository.getReferenceById(u.getId());
        }
        return null;
    }

    private RoleResponse mapToResponse(Role role) {
        Set<PermissionResponse> permissions = role.getPermissions().stream()
                .map(this::mapPermissionToResponse)
                .collect(Collectors.toSet());

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .isSystem(role.getIsSystem())
                .permissions(permissions)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    private PermissionResponse mapPermissionToResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .resource(permission.getResource())
                .action(permission.getAction())
                .name(permission.getName())
                .description(permission.getDescription())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }
}
