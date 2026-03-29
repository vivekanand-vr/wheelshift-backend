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
import com.wheelshiftpro.service.rbac.DataScopeService;
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
 * Service implementation for Employee Data Scope operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class DataScopeServiceImpl implements DataScopeService {

    private final EmployeeDataScopeRepository dataScopeRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;
    private final NotificationEventHelper notificationEventHelper;

    @Override
    public DataScopeResponse addScopeToEmployee(Long employeeId, DataScopeRequest request) {
        log.info("Adding scope {} to employee {}", request.getScopeType(), employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        // Check if scope already exists
        EmployeeDataScope existing = dataScopeRepository.findByEmployeeIdAndScopeTypeAndScopeValue(
                employeeId, request.getScopeType(), request.getScopeValue());

        if (existing != null) {
            throw new DuplicateResourceException(
                    "DataScope", request.getScopeType() + ":" + request.getScopeValue(), employeeId);
        }

        EmployeeDataScope scope = EmployeeDataScope.builder()
                .employee(employee)
                .scopeType(request.getScopeType())
                .scopeValue(request.getScopeValue())
                .effect(request.getEffect())
                .description(request.getDescription())
                .build();

        scope = dataScopeRepository.save(scope);
        log.info("Scope added successfully with ID: {}", scope.getId());

        auditService.log(AuditCategory.EMPLOYEE, scope.getId(), "CREATE_DATA_SCOPE",
                AuditLevel.CRITICAL, resolveCurrentEmployee(),
                "Employee " + employeeId + " — " + request.getScopeType() + " (" + request.getEffect() + ")");

        notificationEventHelper.notifyEmployee(employeeId, NotificationEventType.DATA_SCOPE_CHANGED,
                "EmployeeDataScope", scope.getId(), java.util.Map.of(
                        "scopeType", request.getScopeType().name(),
                        "effect", request.getEffect().name()));

        return mapToResponse(scope);
    }

    @Override
    public DataScopeResponse updateScope(Long scopeId, DataScopeRequest request) {
        log.info("Updating scope with ID: {}", scopeId);

        EmployeeDataScope scope = dataScopeRepository.findById(scopeId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeDataScope", "id", scopeId));

        scope.setEffect(request.getEffect());
        scope.setDescription(request.getDescription());
        scope = dataScopeRepository.save(scope);

        log.info("Scope updated successfully: {}", scopeId);

        auditService.log(AuditCategory.EMPLOYEE, scopeId, "UPDATE_DATA_SCOPE",
                AuditLevel.CRITICAL, resolveCurrentEmployee(),
                "Scope " + scope.getScopeType() + " updated to effect " + scope.getEffect());

        return mapToResponse(scope);
    }

    @Override
    public void removeScopeFromEmployee(Long scopeId) {
        log.info("Removing scope with ID: {}", scopeId);

        EmployeeDataScope scope = dataScopeRepository.findById(scopeId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeDataScope", "id", scopeId));

        Long employeeId = scope.getEmployee().getId();
        dataScopeRepository.delete(scope);
        log.info("Scope removed successfully: {}", scopeId);

        auditService.log(AuditCategory.EMPLOYEE, scopeId, "DELETE_DATA_SCOPE",
                AuditLevel.CRITICAL, resolveCurrentEmployee(),
                "Scope " + scope.getScopeType() + " removed from employee " + employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<DataScopeResponse> getScopesByEmployeeId(Long employeeId) {
        log.debug("Fetching scopes for employee ID: {}", employeeId);
        return dataScopeRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public DataScopeResponse getScopeById(Long scopeId) {
        log.debug("Fetching scope with ID: {}", scopeId);
        EmployeeDataScope scope = dataScopeRepository.findById(scopeId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeDataScope", "id", scopeId));
        return mapToResponse(scope);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataScopeResponse> getScopesByEmployeeIdAndType(Long employeeId, ScopeType scopeType) {
        log.debug("Fetching {} scopes for employee ID: {}", scopeType, employeeId);
        return dataScopeRepository.findByEmployeeIdAndScopeType(employeeId, scopeType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasScope(Long employeeId, ScopeType scopeType, String scopeValue) {
        log.debug("Checking if employee {} has scope {}:{}", employeeId, scopeType, scopeValue);

        EmployeeDataScope scope = dataScopeRepository.findByEmployeeIdAndScopeTypeAndScopeValue(
                employeeId, scopeType, scopeValue);

        return scope != null && scope.getEffect() == ScopeEffect.INCLUDE;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getLocationScopes(Long employeeId) {
        log.debug("Fetching location scopes for employee ID: {}", employeeId);
        return dataScopeRepository.findByEmployeeIdAndScopeType(employeeId, ScopeType.LOCATION).stream()
                .filter(scope -> scope.getEffect() == ScopeEffect.INCLUDE)
                .map(EmployeeDataScope::getScopeValue)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getDepartmentScopes(Long employeeId) {
        log.debug("Fetching department scopes for employee ID: {}", employeeId);
        return dataScopeRepository.findByEmployeeIdAndScopeType(employeeId, ScopeType.DEPARTMENT).stream()
                .filter(scope -> scope.getEffect() == ScopeEffect.INCLUDE)
                .map(EmployeeDataScope::getScopeValue)
                .collect(Collectors.toSet());
    }

    private Employee resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EmployeeUserDetails u) {
            return employeeRepository.getReferenceById(u.getId());
        }
        return null;
    }

    private DataScopeResponse mapToResponse(EmployeeDataScope scope) {
        return DataScopeResponse.builder()
                .id(scope.getId())
                .employeeId(scope.getEmployee().getId())
                .scopeType(scope.getScopeType())
                .scopeValue(scope.getScopeValue())
                .effect(scope.getEffect())
                .description(scope.getDescription())
                .createdAt(scope.getCreatedAt())
                .updatedAt(scope.getUpdatedAt())
                .build();
    }
}
