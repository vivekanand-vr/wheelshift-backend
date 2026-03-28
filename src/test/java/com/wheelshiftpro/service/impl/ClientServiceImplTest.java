package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.ClientRequest;
import com.wheelshiftpro.dto.response.ClientResponse;
import com.wheelshiftpro.entity.Client;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Inquiry;
import com.wheelshiftpro.entity.Reservation;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.ClientStatus;
import com.wheelshiftpro.enums.InquiryStatus;
import com.wheelshiftpro.enums.ReservationStatus;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.ClientMapper;
import com.wheelshiftpro.repository.ClientRepository;
import com.wheelshiftpro.repository.EmployeeRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock ClientRepository clientRepository;
    @Mock ClientMapper clientMapper;
    @Mock EmployeeRepository employeeRepository;
    @Mock AuditService auditService;

    @InjectMocks
    ClientServiceImpl clientService;

    private void setUpAuthenticatedEmployee(Long employeeId) {
        Employee employee = new Employee();
        employee.setId(employeeId);
        EmployeeUserDetails principal = mock(EmployeeUserDetails.class);
        when(principal.getId()).thenReturn(employeeId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
        when(employeeRepository.getReferenceById(employeeId)).thenReturn(employee);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createClient
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createClient")
    class CreateClient {

        @Test
        void happyPath_savesAndAudits() {
            ClientRequest request = request("alice@example.com");
            Client entity = clientWithId(1L, "alice@example.com");
            ClientResponse response = new ClientResponse();
            response.setId(1L);

            when(clientRepository.existsByEmail("alice@example.com")).thenReturn(false);
            when(clientMapper.toEntity(request)).thenReturn(entity);
            when(clientRepository.save(entity)).thenReturn(entity);
            when(clientMapper.toResponse(entity)).thenReturn(response);

            ClientResponse result = clientService.createClient(request);

            assertThat(result.getId()).isEqualTo(1L);
            verify(auditService).log(eq(AuditCategory.CLIENT), eq(1L), eq("CREATE"),
                    eq(AuditLevel.REGULAR), isNull(), anyString());
        }

        @Test
        void duplicateEmail_throwsDuplicateResourceException() {
            ClientRequest request = request("alice@example.com");
            when(clientRepository.existsByEmail("alice@example.com")).thenReturn(true);

            assertThatThrownBy(() -> clientService.createClient(request))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(auditService, never()).log(any(), any(), any(), any(), any(), any());
        }

        @Test
        void auditLevel_isRegular() {
            ClientRequest request = request("b@b.com");
            Client entity = clientWithId(2L, "b@b.com");

            when(clientRepository.existsByEmail("b@b.com")).thenReturn(false);
            when(clientMapper.toEntity(request)).thenReturn(entity);
            when(clientRepository.save(entity)).thenReturn(entity);
            when(clientMapper.toResponse(entity)).thenReturn(new ClientResponse());

            clientService.createClient(request);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        void auditField_populatedWhenAuthenticated() {
            setUpAuthenticatedEmployee(10L);

            ClientRequest request = request("c@c.com");
            Client entity = clientWithId(3L, "c@c.com");

            when(clientRepository.existsByEmail("c@c.com")).thenReturn(false);
            when(clientMapper.toEntity(request)).thenReturn(entity);
            when(clientRepository.save(entity)).thenReturn(entity);
            when(clientMapper.toResponse(entity)).thenReturn(new ClientResponse());

            clientService.createClient(request);

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), captor.capture(), any());
            assertThat(captor.getValue()).isNotNull();
            assertThat(captor.getValue().getId()).isEqualTo(10L);
        }

        @Test
        void auditField_nullWhenNotAuthenticated() {
            ClientRequest request = request("d@d.com");
            Client entity = clientWithId(4L, "d@d.com");

            when(clientRepository.existsByEmail("d@d.com")).thenReturn(false);
            when(clientMapper.toEntity(request)).thenReturn(entity);
            when(clientRepository.save(entity)).thenReturn(entity);
            when(clientMapper.toResponse(entity)).thenReturn(new ClientResponse());

            clientService.createClient(request);

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), captor.capture(), any());
            assertThat(captor.getValue()).isNull();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateClient
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateClient")
    class UpdateClient {

        @Test
        void happyPath_updatesAndAudits() {
            ClientRequest request = request("new@example.com");
            Client entity = clientWithId(1L, "old@example.com");

            when(clientRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(clientRepository.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(false);
            when(clientRepository.save(entity)).thenReturn(entity);
            when(clientMapper.toResponse(entity)).thenReturn(new ClientResponse());

            clientService.updateClient(1L, request);

            verify(clientMapper).updateEntityFromRequest(request, entity);
            verify(auditService).log(eq(AuditCategory.CLIENT), eq(1L), eq("UPDATE"),
                    eq(AuditLevel.REGULAR), isNull(), anyString());
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.updateClient(99L, request("x@x.com")))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void duplicateEmailOnUpdate_throwsDuplicateResourceException() {
            ClientRequest request = request("taken@example.com");
            Client entity = clientWithId(1L, "old@example.com");

            when(clientRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(clientRepository.existsByEmailAndIdNot("taken@example.com", 1L)).thenReturn(true);

            assertThatThrownBy(() -> clientService.updateClient(1L, request))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        void nullEmail_skipsUniquenessCheck() {
            ClientRequest request = new ClientRequest();  // null email
            Client entity = clientWithId(1L, "old@example.com");

            when(clientRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(clientRepository.save(entity)).thenReturn(entity);
            when(clientMapper.toResponse(entity)).thenReturn(new ClientResponse());

            clientService.updateClient(1L, request);

            verify(clientRepository, never()).existsByEmailAndIdNot(any(), any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteClient
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteClient")
    class DeleteClient {

        @Test
        void happyPath_deletesAndAudits() {
            Client entity = clientWithNoRelations(1L, "a@a.com");

            when(clientRepository.findById(1L)).thenReturn(Optional.of(entity));

            clientService.deleteClient(1L);

            verify(clientRepository).delete(entity);
            verify(auditService).log(eq(AuditCategory.CLIENT), eq(1L), eq("DELETE"),
                    eq(AuditLevel.HIGH), isNull(), anyString());
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.deleteClient(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void hasOpenInquiry_throwsBusinessException() {
            Client client = clientWithOpenInquiry(1L);
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

            assertThatThrownBy(() -> clientService.deleteClient(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("open inquiries");
        }

        @Test
        void hasActiveReservation_throwsBusinessException() {
            Client client = clientWithActiveReservation(1L);
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

            assertThatThrownBy(() -> clientService.deleteClient(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("active reservations");
        }

        @Test
        void auditLevel_isHigh() {
            Client entity = clientWithNoRelations(1L, "a@a.com");
            when(clientRepository.findById(1L)).thenReturn(Optional.of(entity));

            clientService.deleteClient(1L);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.HIGH), any(), any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateClientStatus
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateClientStatus")
    class UpdateClientStatus {

        @Test
        void happyPath_updatesStatusAndAudits() {
            Client entity = clientWithId(1L, "a@a.com");
            entity.setStatus(ClientStatus.ACTIVE);

            when(clientRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(clientRepository.save(entity)).thenReturn(entity);
            when(clientMapper.toResponse(entity)).thenReturn(new ClientResponse());

            clientService.updateClientStatus(1L, ClientStatus.INACTIVE);

            assertThat(entity.getStatus()).isEqualTo(ClientStatus.INACTIVE);
            verify(auditService).log(eq(AuditCategory.CLIENT), eq(1L), eq("STATUS_CHANGE"),
                    eq(AuditLevel.HIGH), isNull(), contains("ACTIVE"));
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.updateClientStatus(99L, ClientStatus.INACTIVE))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void auditLevel_isHigh() {
            Client entity = clientWithId(1L, "a@a.com");
            entity.setStatus(ClientStatus.ACTIVE);

            when(clientRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(clientRepository.save(entity)).thenReturn(entity);
            when(clientMapper.toResponse(entity)).thenReturn(new ClientResponse());

            clientService.updateClientStatus(1L, ClientStatus.INACTIVE);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.HIGH), any(), any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getClientById
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getClientById")
    class GetClientById {

        @Test
        void happyPath_returnsResponse() {
            Client entity = clientWithId(1L, "a@a.com");
            ClientResponse response = new ClientResponse();
            response.setId(1L);

            when(clientRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(clientMapper.toResponse(entity)).thenReturn(response);

            assertThat(clientService.getClientById(1L).getId()).isEqualTo(1L);
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.getClientById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // incrementPurchaseCount
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("incrementPurchaseCount")
    class IncrementPurchaseCount {

        @Test
        void happyPath_incrementsAndSetsLastPurchase() {
            Client entity = clientWithId(1L, "a@a.com");
            entity.setTotalPurchases(2);

            when(clientRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(clientRepository.save(entity)).thenReturn(entity);

            clientService.incrementPurchaseCount(1L);

            assertThat(entity.getTotalPurchases()).isEqualTo(3);
            assertThat(entity.getLastPurchase()).isNotNull();
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.incrementPurchaseCount(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllClients
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllClients")
    class GetAllClients {

        @Test
        void happyPath_returnsPaginatedResponse() {
            Page<Client> page = new PageImpl<>(List.of());
            when(clientRepository.findAll(any(Pageable.class))).thenReturn(page);
            when(clientMapper.toResponseList(List.of())).thenReturn(List.of());

            var result = clientService.getAllClients(0, 20);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private ClientRequest request(String email) {
        ClientRequest r = new ClientRequest();
        r.setEmail(email);
        return r;
    }

    private Client clientWithId(Long id, String email) {
        Client c = new Client();
        c.setId(id);
        c.setEmail(email);
        c.setInquiries(new ArrayList<>());
        c.setReservations(new ArrayList<>());
        return c;
    }

    private Client clientWithNoRelations(Long id, String email) {
        return clientWithId(id, email);
    }

    private Client clientWithOpenInquiry(Long id) {
        Client c = clientWithId(id, "a@a.com");
        Inquiry inquiry = new Inquiry();
        inquiry.setStatus(InquiryStatus.OPEN);
        c.getInquiries().add(inquiry);
        return c;
    }

    private Client clientWithActiveReservation(Long id) {
        Client c = clientWithId(id, "a@a.com");
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.CONFIRMED);
        c.getReservations().add(reservation);
        return c;
    }
}
