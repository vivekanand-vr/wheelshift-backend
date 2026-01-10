package com.wheelshiftpro.service.impl.rbac;

import com.wheelshiftpro.dto.request.rbac.PermissionRequest;
import com.wheelshiftpro.dto.response.rbac.PermissionResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.rbac.Permission;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.rbac.PermissionRepository;
import com.wheelshiftpro.service.rbac.PermissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation for Permission operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public PermissionResponse createPermission(PermissionRequest request) {
        log.info("Creating new permission: {}:{}", request.getResource(), request.getAction());

        String permissionName = Permission.buildPermissionName(request.getResource(), request.getAction());

        if (permissionRepository.existsByName(permissionName)) {
            throw new IllegalArgumentException("Permission already exists: " + permissionName);
        }

        Permission permission = Permission.builder()
                .resource(request.getResource())
                .action(request.getAction())
                .name(permissionName)
                .description(request.getDescription())
                .build();

        permission = permissionRepository.save(permission);
        log.info("Permission created successfully with ID: {}", permission.getId());

        return mapToResponse(permission);
    }

    @Override
    public PermissionResponse updatePermission(Long permissionId, PermissionRequest request) {
        log.info("Updating permission with ID: {}", permissionId);

        Permission permission = getPermissionEntityById(permissionId);

        permission.setDescription(request.getDescription());
        permission = permissionRepository.save(permission);

        log.info("Permission updated successfully: {}", permissionId);
        return mapToResponse(permission);
    }

    @Override
    public void deletePermission(Long permissionId) {
        log.info("Deleting permission with ID: {}", permissionId);

        Permission permission = getPermissionEntityById(permissionId);

        if (!permission.getRoles().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete permission that is assigned to roles");
        }

        permissionRepository.delete(permission);
        log.info("Permission deleted successfully: {}", permissionId);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(Long permissionId) {
        log.debug("Fetching permission with ID: {}", permissionId);
        return mapToResponse(getPermissionEntityById(permissionId));
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermissionByName(String name) {
        log.debug("Fetching permission with name: {}", name);
        Permission permission = permissionRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with name: " + name));
        return mapToResponse(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        log.debug("Fetching all permissions");
        return permissionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<PermissionResponse> getPermissionsByRoleId(Long roleId) {
        log.debug("Fetching permissions for role ID: {}", roleId);
        return permissionRepository.findByRoleId(roleId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<PermissionResponse> getPermissionsByEmployeeId(Long employeeId) {
        log.debug("Fetching permissions for employee ID: {}", employeeId);
        return permissionRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Permission getPermissionEntityById(Long permissionId) {
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + permissionId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long employeeId, String permissionName) {
        log.debug("Checking if employee {} has permission {}", employeeId, permissionName);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));
        log.atInfo().log("Employee found: {} with email: {}", employee.getId(), employee.getEmail());
        Set<Permission> permissions = permissionRepository.findByEmployeeId(employeeId);

        return permissions.stream()
                .anyMatch(p -> p.getName().equals(permissionName));
    }

    private PermissionResponse mapToResponse(Permission permission) {
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
