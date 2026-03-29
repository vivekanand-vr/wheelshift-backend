package com.wheelshiftpro.service.impl.rbac;

import com.wheelshiftpro.dto.request.rbac.ResourceACLRequest;
import com.wheelshiftpro.dto.response.rbac.ResourceACLResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.rbac.ResourceACL;
import com.wheelshiftpro.entity.rbac.Role;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.rbac.AccessLevel;
import com.wheelshiftpro.enums.rbac.ResourceType;
import com.wheelshiftpro.enums.rbac.RoleType;
import com.wheelshiftpro.enums.rbac.SubjectType;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.rbac.ResourceACLRepository;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResourceACLServiceImpl Tests")
class ResourceACLServiceImplTest {

    @Mock private ResourceACLRepository aclRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private AuditService auditService;

    @InjectMocks private ResourceACLServiceImpl aclService;

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

    private ResourceACLRequest buildRequest(SubjectType subjectType, Long subjectId, AccessLevel access) {
        ResourceACLRequest request = new ResourceACLRequest();
        request.setSubjectType(subjectType);
        request.setSubjectId(subjectId);
        request.setAccess(access);
        request.setReason("test reason");
        return request;
    }

    private ResourceACL buildACL(Long id, ResourceType resourceType, Long resourceId,
                                  SubjectType subjectType, Long subjectId, AccessLevel access) {
        return ResourceACL.builder()
                .id(id)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .subjectType(subjectType)
                .subjectId(subjectId)
                .access(access)
                .reason("test reason")
                .grantedBy(1L)
                .build();
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("addACL")
    class AddACL {

        @Test
        @DisplayName("should add ACL entry and log CRITICAL audit")
        void happyPath() {
            ResourceACLRequest request = buildRequest(SubjectType.EMPLOYEE, 5L, AccessLevel.READ);

            when(aclRepository.findByResourceTypeAndResourceIdAndSubjectTypeAndSubjectIdAndAccess(
                    ResourceType.CAR, 100L, SubjectType.EMPLOYEE, 5L, AccessLevel.READ))
                    .thenReturn(Optional.empty());

            ResourceACL saved = buildACL(20L, ResourceType.CAR, 100L, SubjectType.EMPLOYEE, 5L, AccessLevel.READ);
            when(aclRepository.save(any(ResourceACL.class))).thenReturn(saved);

            ResourceACLResponse result = aclService.addACL(ResourceType.CAR, 100L, request, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getResourceType()).isEqualTo(ResourceType.CAR);
            assertThat(result.getAccess()).isEqualTo(AccessLevel.READ);

            verify(auditService).log(eq(AuditCategory.SYSTEM), eq(20L), eq("CREATE_ACL"),
                    eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when identical ACL already exists")
        void duplicateACL_throws() {
            ResourceACLRequest request = buildRequest(SubjectType.EMPLOYEE, 5L, AccessLevel.READ);

            ResourceACL existing = buildACL(10L, ResourceType.CAR, 100L, SubjectType.EMPLOYEE, 5L, AccessLevel.READ);
            when(aclRepository.findByResourceTypeAndResourceIdAndSubjectTypeAndSubjectIdAndAccess(
                    ResourceType.CAR, 100L, SubjectType.EMPLOYEE, 5L, AccessLevel.READ))
                    .thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> aclService.addACL(ResourceType.CAR, 100L, request, 1L))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(aclRepository, never()).save(any());
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("audit level must be CRITICAL")
        void auditLevelCritical() {
            ResourceACLRequest request = buildRequest(SubjectType.ROLE, 2L, AccessLevel.WRITE);

            when(aclRepository.findByResourceTypeAndResourceIdAndSubjectTypeAndSubjectIdAndAccess(
                    any(), any(), any(), any(), any())).thenReturn(Optional.empty());
            when(aclRepository.save(any())).thenReturn(buildACL(1L, ResourceType.CLIENT, 1L, SubjectType.ROLE, 2L, AccessLevel.WRITE));

            aclService.addACL(ResourceType.CLIENT, 1L, request, 1L);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("audit performedBy is populated when authenticated")
        void auditFieldPopulatedWhenAuthenticated() {
            setUpAuthenticatedEmployee(50L);

            ResourceACLRequest request = buildRequest(SubjectType.EMPLOYEE, 5L, AccessLevel.READ);
            when(aclRepository.findByResourceTypeAndResourceIdAndSubjectTypeAndSubjectIdAndAccess(
                    any(), any(), any(), any(), any())).thenReturn(Optional.empty());
            when(aclRepository.save(any())).thenReturn(buildACL(1L, ResourceType.CAR, 1L, SubjectType.EMPLOYEE, 5L, AccessLevel.READ));

            aclService.addACL(ResourceType.CAR, 1L, request, 1L);

            ArgumentCaptor<Employee> empCaptor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), empCaptor.capture(), any());
            assertThat(empCaptor.getValue()).isNotNull();
            assertThat(empCaptor.getValue().getId()).isEqualTo(50L);
        }

        @Test
        @DisplayName("audit performedBy is null when unauthenticated")
        void auditFieldNullWhenUnauthenticated() {
            ResourceACLRequest request = buildRequest(SubjectType.EMPLOYEE, 5L, AccessLevel.READ);
            when(aclRepository.findByResourceTypeAndResourceIdAndSubjectTypeAndSubjectIdAndAccess(
                    any(), any(), any(), any(), any())).thenReturn(Optional.empty());
            when(aclRepository.save(any())).thenReturn(buildACL(1L, ResourceType.CAR, 1L, SubjectType.EMPLOYEE, 5L, AccessLevel.READ));

            aclService.addACL(ResourceType.CAR, 1L, request, 1L);

            ArgumentCaptor<Employee> empCaptor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), empCaptor.capture(), any());
            assertThat(empCaptor.getValue()).isNull();
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("removeACL")
    class RemoveACL {

        @Test
        @DisplayName("should remove ACL entry and log CRITICAL audit")
        void happyPath() {
            ResourceACL acl = buildACL(20L, ResourceType.CAR, 100L, SubjectType.EMPLOYEE, 5L, AccessLevel.READ);
            when(aclRepository.findById(20L)).thenReturn(Optional.of(acl));

            aclService.removeACL(20L);

            verify(aclRepository).delete(acl);
            verify(auditService).log(eq(AuditCategory.SYSTEM), eq(20L), eq("DELETE_ACL"),
                    eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when ACL entry not found")
        void notFound_throws() {
            when(aclRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> aclService.removeACL(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ResourceACL");

            verify(aclRepository, never()).delete(any());
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("audit level must be CRITICAL for delete")
        void auditLevelCritical() {
            ResourceACL acl = buildACL(5L, ResourceType.CLIENT, 1L, SubjectType.ROLE, 2L, AccessLevel.WRITE);
            when(aclRepository.findById(5L)).thenReturn(Optional.of(acl));

            aclService.removeACL(5L);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.CRITICAL), any(), any());
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getACLByResource")
    class GetACLByResource {

        @Test
        @DisplayName("should return all ACL entries for a resource")
        void happyPath() {
            ResourceACL acl1 = buildACL(1L, ResourceType.CAR, 100L, SubjectType.EMPLOYEE, 5L, AccessLevel.READ);
            ResourceACL acl2 = buildACL(2L, ResourceType.CAR, 100L, SubjectType.ROLE, 3L, AccessLevel.WRITE);

            when(aclRepository.findByResourceTypeAndResourceId(ResourceType.CAR, 100L))
                    .thenReturn(List.of(acl1, acl2));

            List<ResourceACLResponse> result = aclService.getACLByResource(ResourceType.CAR, 100L);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should return empty list when no ACL entries exist")
        void emptyList() {
            when(aclRepository.findByResourceTypeAndResourceId(ResourceType.SALE, 999L))
                    .thenReturn(Collections.emptyList());

            assertThat(aclService.getACLByResource(ResourceType.SALE, 999L)).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("hasACLAccess")
    class HasACLAccess {

        @Test
        @DisplayName("should return true when employee has required ACL access")
        void hasAccess() {
            Role role = Role.builder().id(3L).name(RoleType.SALES).isSystem(false)
                    .permissions(new HashSet<>()).employees(new HashSet<>()).build();
            Employee employee = new Employee();
            employee.setId(1L);
            employee.setRoles(new HashSet<>(Set.of(role)));

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(aclRepository.hasAccess(eq(ResourceType.CAR), eq(100L), eq(1L), any(), eq(AccessLevel.READ)))
                    .thenReturn(true);

            assertThat(aclService.hasACLAccess(ResourceType.CAR, 100L, 1L, AccessLevel.READ)).isTrue();
        }

        @Test
        @DisplayName("should return false when employee has no ACL access")
        void noAccess() {
            Employee employee = new Employee();
            employee.setId(1L);
            employee.setRoles(new HashSet<>());

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(aclRepository.hasAccess(any(), any(), any(), any(), any())).thenReturn(false);

            assertThat(aclService.hasACLAccess(ResourceType.CAR, 100L, 1L, AccessLevel.ADMIN)).isFalse();
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when employee not found")
        void employeeNotFound_throws() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> aclService.hasACLAccess(ResourceType.CAR, 100L, 99L, AccessLevel.READ))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("removeAllACLForResource")
    class RemoveAllACLForResource {

        @Test
        @DisplayName("should delete all ACL entries for resource and log CRITICAL audit")
        void happyPath() {
            aclService.removeAllACLForResource(ResourceType.CAR, 100L);

            verify(aclRepository).deleteByResourceTypeAndResourceId(ResourceType.CAR, 100L);
            verify(auditService).log(eq(AuditCategory.SYSTEM), eq(100L), eq("DELETE_ALL_ACL"),
                    eq(AuditLevel.CRITICAL), any(), any());
        }

        @Test
        @DisplayName("audit level must be CRITICAL")
        void auditLevelCritical() {
            aclService.removeAllACLForResource(ResourceType.SALE, 50L);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.CRITICAL), any(), any());
        }
    }
}
