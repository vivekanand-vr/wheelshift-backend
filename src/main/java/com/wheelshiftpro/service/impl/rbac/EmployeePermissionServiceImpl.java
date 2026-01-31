package com.wheelshiftpro.service.impl.rbac;

import com.wheelshiftpro.dto.request.rbac.EmployeePermissionRequest;
import com.wheelshiftpro.dto.response.rbac.EmployeePermissionResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.rbac.EmployeePermission;
import com.wheelshiftpro.entity.rbac.Permission;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.rbac.EmployeePermissionMapper;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.rbac.EmployeePermissionRepository;
import com.wheelshiftpro.repository.rbac.PermissionRepository;
import com.wheelshiftpro.service.rbac.EmployeePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of EmployeePermissionService for managing custom employee permissions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeePermissionServiceImpl implements EmployeePermissionService {

    private final EmployeePermissionRepository employeePermissionRepository;
    private final EmployeeRepository employeeRepository;
    private final PermissionRepository permissionRepository;
    private final EmployeePermissionMapper employeePermissionMapper;

    @Override
    @Transactional
    public EmployeePermissionResponse assignPermissionToEmployee(Long employeeId, 
                                                                 EmployeePermissionRequest request,
                                                                 Long grantedBy) {
        log.info("Assigning permission {} to employee {} by admin {}", 
                request.getPermissionId(), employeeId, grantedBy);

        // Validate employee exists
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        // Validate permission exists
        Permission permission = permissionRepository.findById(request.getPermissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + request.getPermissionId()));

        // Check if permission already assigned
        if (employeePermissionRepository.existsByEmployeeIdAndPermissionId(employeeId, request.getPermissionId())) {
            throw new DuplicateResourceException("Permission " + permission.getName() + 
                    " is already assigned to employee " + employee.getName());
        }

        // Validate granted by admin exists
        Employee grantedByAdmin = employeeRepository.findById(grantedBy)
                .orElseThrow(() -> new ResourceNotFoundException("Granting admin not found with id: " + grantedBy));

        // Create employee permission
        EmployeePermission employeePermission = new EmployeePermission();
        employeePermission.setEmployee(employee);
        employeePermission.setPermission(permission);
        employeePermission.setGrantedBy(grantedByAdmin);
        employeePermission.setReason(request.getReason());

        EmployeePermission saved = employeePermissionRepository.save(employeePermission);
        
        log.info("Successfully assigned custom permission {} to employee {}", 
                permission.getName(), employeeId);

        return employeePermissionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void removePermissionFromEmployee(Long employeeId, Long permissionId) {
        log.info("Removing custom permission {} from employee {}", permissionId, employeeId);

        EmployeePermission employeePermission = employeePermissionRepository
                .findByEmployeeIdAndPermissionId(employeeId, permissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Custom permission assignment not found for employee " + employeeId + 
                        " and permission " + permissionId));

        employeePermissionRepository.delete(employeePermission);
        
        log.info("Successfully removed custom permission {} from employee {}", permissionId, employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeePermissionResponse> getEmployeeCustomPermissions(Long employeeId) {
        log.debug("Fetching custom permissions for employee {}", employeeId);

        // Validate employee exists
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }

        List<EmployeePermission> permissions = employeePermissionRepository.findByEmployeeId(employeeId);
        
        return permissions.stream()
                .map(employeePermissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getEmployeeCustomPermissionNames(Long employeeId) {
        log.debug("Fetching custom permission names for employee {}", employeeId);
        return employeePermissionRepository.findPermissionNamesByEmployeeId(employeeId);
    }

    @Override
    @Transactional
    public void removeAllCustomPermissions(Long employeeId) {
        log.info("Removing all custom permissions from employee {}", employeeId);

        // Validate employee exists
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }

        int deletedCount = employeePermissionRepository.deleteByEmployeeId(employeeId);
        log.info("Removed {} custom permissions from employee {}", deletedCount, employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePermissionResponse getEmployeePermissionById(Long id) {
        log.debug("Fetching employee permission by id {}", id);

        EmployeePermission employeePermission = employeePermissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee permission not found with id: " + id));

        return employeePermissionMapper.toResponse(employeePermission);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasCustomPermission(Long employeeId, String permissionName) {
        Set<String> customPermissions = getEmployeeCustomPermissionNames(employeeId);
        return customPermissions.contains(permissionName);
    }
}
