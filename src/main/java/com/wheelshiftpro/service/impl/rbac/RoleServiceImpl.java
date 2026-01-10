package com.wheelshiftpro.service.impl.rbac;

import com.wheelshiftpro.dto.request.rbac.RoleRequest;
import com.wheelshiftpro.dto.response.rbac.PermissionResponse;
import com.wheelshiftpro.dto.response.rbac.RoleResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.rbac.Permission;
import com.wheelshiftpro.entity.rbac.Role;
import com.wheelshiftpro.enums.rbac.RoleType;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.rbac.PermissionRepository;
import com.wheelshiftpro.repository.rbac.RoleRepository;
import com.wheelshiftpro.service.rbac.RoleService;

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
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public RoleResponse createRole(RoleRequest request) {
        log.info("Creating new role: {}", request.getName());

        if (roleRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Role with name " + request.getName() + " already exists");
        }

        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isSystem(request.getIsSystem() != null ? request.getIsSystem() : false)
                .build();

        role = roleRepository.save(role);
        log.info("Role created successfully with ID: {}", role.getId());

        return mapToResponse(role);
    }

    @Override
    public RoleResponse updateRole(Long roleId, RoleRequest request) {
        log.info("Updating role with ID: {}", roleId);

        Role role = getRoleEntityById(roleId);

        if (role.getIsSystem()) {
            throw new IllegalArgumentException("Cannot update system role");
        }

        role.setDescription(request.getDescription());
        role = roleRepository.save(role);

        log.info("Role updated successfully: {}", roleId);
        return mapToResponse(role);
    }

    @Override
    public void deleteRole(Long roleId) {
        log.info("Deleting role with ID: {}", roleId);

        Role role = getRoleEntityById(roleId);

        if (role.getIsSystem()) {
            throw new IllegalArgumentException("Cannot delete system role");
        }

        if (!role.getEmployees().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete role that is assigned to employees");
        }

        roleRepository.delete(role);
        log.info("Role deleted successfully: {}", roleId);
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
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
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
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        Role role = getRoleEntityById(roleId);

        employee.addRole(role);
        employeeRepository.save(employee);

        log.info("Role assigned successfully");
    }

    @Override
    public void removeRoleFromEmployee(Long employeeId, Long roleId) {
        log.info("Removing role {} from employee {}", roleId, employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        Role role = getRoleEntityById(roleId);

        employee.removeRole(role);
        employeeRepository.save(employee);

        log.info("Role removed successfully");
    }

    @Override
    public void addPermissionToRole(Long roleId, Long permissionId) {
        log.info("Adding permission {} to role {}", permissionId, roleId);

        Role role = getRoleEntityById(roleId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + permissionId));

        role.addPermission(permission);
        roleRepository.save(role);

        log.info("Permission added successfully");
    }

    @Override
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        log.info("Removing permission {} from role {}", permissionId, roleId);

        Role role = getRoleEntityById(roleId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + permissionId));

        role.removePermission(permission);
        roleRepository.save(role);

        log.info("Permission removed successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleEntityById(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleEntityByName(RoleType name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
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
