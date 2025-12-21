package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.DataScopeRequest;
import com.wheelshiftpro.dto.response.DataScopeResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.EmployeeDataScope;
import com.wheelshiftpro.enums.ScopeEffect;
import com.wheelshiftpro.enums.ScopeType;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.repository.EmployeeDataScopeRepository;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.service.DataScopeService;
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
@Transactional
public class DataScopeServiceImpl implements DataScopeService {

    private final EmployeeDataScopeRepository dataScopeRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public DataScopeResponse addScopeToEmployee(Long employeeId, DataScopeRequest request) {
        log.info("Adding scope {} to employee {}", request.getScopeType(), employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        // Check if scope already exists
        EmployeeDataScope existing = dataScopeRepository.findByEmployeeIdAndScopeTypeAndScopeValue(
                employeeId, request.getScopeType(), request.getScopeValue());

        if (existing != null) {
            throw new IllegalArgumentException("Scope already exists for this employee");
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

        return mapToResponse(scope);
    }

    @Override
    public DataScopeResponse updateScope(Long scopeId, DataScopeRequest request) {
        log.info("Updating scope with ID: {}", scopeId);

        EmployeeDataScope scope = dataScopeRepository.findById(scopeId)
                .orElseThrow(() -> new ResourceNotFoundException("Scope not found with ID: " + scopeId));

        scope.setEffect(request.getEffect());
        scope.setDescription(request.getDescription());
        scope = dataScopeRepository.save(scope);

        log.info("Scope updated successfully: {}", scopeId);
        return mapToResponse(scope);
    }

    @Override
    public void removeScopeFromEmployee(Long scopeId) {
        log.info("Removing scope with ID: {}", scopeId);

        EmployeeDataScope scope = dataScopeRepository.findById(scopeId)
                .orElseThrow(() -> new ResourceNotFoundException("Scope not found with ID: " + scopeId));

        dataScopeRepository.delete(scope);
        log.info("Scope removed successfully: {}", scopeId);
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
