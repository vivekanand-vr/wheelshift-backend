package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.EmployeeRequest;
import com.wheelshiftpro.dto.response.EmployeeResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.entity.Employee;
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
import com.wheelshiftpro.service.EmployeeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SaleRepository saleRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.debug("Creating employee: {}", request.getName());

        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Employee", "email", request.getEmail());
        }

        Employee employee = employeeMapper.toEntity(request);
        
        // Hash the password before saving
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(request.getPassword());
            employee.setPasswordHash(hashedPassword);
            log.debug("Password hashed for new employee");
        }
        
        Employee saved = employeeRepository.save(employee);

        auditService.log(AuditCategory.EMPLOYEE, saved.getId(), "CREATE",
                AuditLevel.CRITICAL, resolveCurrentEmployee(), "Email: " + saved.getEmail());
        log.info("Created employee with ID: {}", saved.getId());
        return employeeMapper.toResponse(saved);
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        log.debug("Updating employee ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        if (request.getEmail() != null &&
                employeeRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("Employee", "email", request.getEmail());
        }

        employeeMapper.updateEntityFromRequest(request, employee);

        // Hash the password if it's being updated
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            log.debug("Password updated and hashed for employee ID: {}", id);
        }

        Employee updated = employeeRepository.save(employee);

        auditService.log(AuditCategory.EMPLOYEE, id, "UPDATE",
                AuditLevel.CRITICAL, resolveCurrentEmployee(), "Email: " + updated.getEmail());
        log.info("Updated employee ID: {}", id);
        return employeeMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        log.debug("Fetching employee ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getAllEmployees(String search, int page, int size) {
        log.debug("Fetching all employees - search: {}, page: {}, size: {}", search, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<Employee> employeesPage;
        
        if (search != null && !search.trim().isEmpty()) {
            employeesPage = employeeRepository.searchEmployeesByPattern(search, pageable);
        } else {
            employeesPage = employeeRepository.findAll(pageable);
        }

        return buildPageResponse(employeesPage);
    }

    @Override
    public void deleteEmployee(Long id) {
        log.debug("Deleting employee ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        // Check if employee has associated sales
        if (!employee.getHandledSales().isEmpty()) {
            throw new BusinessException("Cannot delete employee with associated sales", "EMPLOYEE_HAS_SALES");
        }

        if (!employee.getAssignedTasks().isEmpty()) {
            throw new BusinessException("Cannot delete employee with assigned tasks", "EMPLOYEE_HAS_TASKS");
        }

        boolean isSuperAdmin = employee.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleType.SUPER_ADMIN);
        if (isSuperAdmin && employeeRepository.countByRolesName(RoleType.SUPER_ADMIN) <= 1) {
            throw new BusinessException(
                    "Cannot delete the last SUPER_ADMIN account", "LAST_SUPER_ADMIN");
        }

        employeeRepository.delete(employee);
        auditService.log(AuditCategory.EMPLOYEE, id, "DELETE",
                AuditLevel.CRITICAL, resolveCurrentEmployee(), "Email: " + employee.getEmail());
        log.info("Deleted employee ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> searchEmployees(String name, String role, EmployeeStatus status, 
                                                          int page, int size) {
        log.debug("Searching employees");

        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<Employee> employeesPage = employeeRepository.searchEmployees(name, pageable);

        return buildPageResponse(employeesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getEmployeesByRole(String role, int page, int size) {
        log.debug("Fetching employees by role: {}", role);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        List<Employee> employeesList = employeeRepository.findByPosition(role);
        Page<Employee> employeesPage = new PageImpl<>(employeesList, pageable, employeesList.size());

        return buildPageResponse(employeesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getActiveEmployees(int page, int size) {
        log.debug("Fetching active employees");

        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<Employee> employeesPage = employeeRepository.findByStatus(EmployeeStatus.ACTIVE, pageable);

        return buildPageResponse(employeesPage);
    }

    @Override
    public EmployeeResponse updateEmployeeStatus(Long id, EmployeeStatus status) {
        log.debug("Updating employee ID: {} status to: {}", id, status);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        EmployeeUserDetails currentUser = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EmployeeUserDetails u) {
            currentUser = u;
        }
        if (currentUser != null && currentUser.getId().equals(id)
                && (status == EmployeeStatus.INACTIVE || status == EmployeeStatus.SUSPENDED)) {
            throw new BusinessException(
                    "An employee cannot change their own status to INACTIVE or SUSPENDED",
                    "SELF_STATUS_CHANGE_FORBIDDEN");
        }

        EmployeeStatus previous = employee.getStatus();
        employee.setStatus(status);
        Employee updated = employeeRepository.save(employee);

        auditService.log(AuditCategory.EMPLOYEE, id, "STATUS_CHANGE",
                AuditLevel.CRITICAL, resolveCurrentEmployee(),
                "From " + previous + " to " + status);
        log.info("Updated employee ID: {} status to: {}", id, status);
        return employeeMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getEmployeePerformance(Long id) {
        log.debug("Calculating performance for employee ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        Long totalSales = (long) employee.getHandledSales().size();
        BigDecimal totalRevenue = saleRepository.calculateTotalRevenue(null, null);
        BigDecimal totalCommission = saleRepository.calculateTotalCommission(null, null);

        return Map.of(
                "employeeId", id,
                "employeeName", employee.getName(),
                "totalSales", totalSales,
                "totalRevenue", totalRevenue != null ? totalRevenue.doubleValue() : 0.0,
                "totalCommission", totalCommission != null ? totalCommission.doubleValue() : 0.0
        );
    }

    private Employee resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EmployeeUserDetails u) {
            return employeeRepository.getReferenceById(u.getId());
        }
        return null;
    }

    private PageResponse<EmployeeResponse> buildPageResponse(Page<Employee> page) {
        List<EmployeeResponse> content = employeeMapper.toResponseList(page.getContent());

        return PageResponse.<EmployeeResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }
}
