package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.ClientRequest;
import com.wheelshiftpro.dto.response.ClientResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.entity.Client;
import com.wheelshiftpro.enums.ClientStatus;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.ClientMapper;
import com.wheelshiftpro.repository.ClientRepository;
import com.wheelshiftpro.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    public ClientResponse createClient(ClientRequest request) {
        log.debug("Creating client: {}", request.getName());

        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Client", "email", request.getEmail());
        }

        Client client = clientMapper.toEntity(request);
        Client saved = clientRepository.save(client);

        log.info("Created client with ID: {}", saved.getId());
        return clientMapper.toResponse(saved);
    }

    @Override
    public ClientResponse updateClient(Long id, ClientRequest request) {
        log.debug("Updating client ID: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

        clientMapper.updateEntityFromRequest(request, client);
        Client updated = clientRepository.save(client);

        log.info("Updated client ID: {}", id);
        return clientMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long id) {
        log.debug("Fetching client ID: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientByEmail(String email) {
        log.debug("Fetching client by email: {}", email);

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "email", email));

        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClientResponse> getAllClients(int page, int size) {
        log.debug("Fetching all clients - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<Client> clientsPage = clientRepository.findAll(pageable);

        return buildPageResponse(clientsPage);
    }

    @Override
    public void deleteClient(Long id) {
        log.debug("Deleting client ID: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

        clientRepository.delete(client);
        log.info("Deleted client ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClientResponse> searchClients(String searchTerm, int page, int size) {
        log.debug("Searching clients with term: {}", searchTerm);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<Client> clientsPage = clientRepository.searchClients(searchTerm, pageable);

        return buildPageResponse(clientsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClientResponse> getClientsByStatus(ClientStatus status, int page, int size) {
        log.debug("Fetching clients by status: {}", status);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<Client> clientsPage = clientRepository.findByStatus(status, pageable);

        return buildPageResponse(clientsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> getTopBuyers(int limit) {
        log.debug("Fetching top {} buyers", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Client> topBuyers = clientRepository.findTopBuyers(pageable);

        return clientMapper.toResponseList(topBuyers);
    }

    @Override
    public void incrementPurchaseCount(Long clientId) {
        log.debug("Incrementing purchase count for client ID: {}", clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));

        client.setTotalPurchases(client.getTotalPurchases() + 1);
        client.setLastPurchase(LocalDate.now());
        clientRepository.save(client);

        log.info("Incremented purchase count for client ID: {}", clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getClientStatistics() {
        log.debug("Calculating overall client statistics");

        long totalClients = clientRepository.count();
        long activeClients = clientRepository.findByStatus(ClientStatus.ACTIVE, Pageable.unpaged()).getTotalElements();

        return Map.of(
                "totalClients", totalClients,
                "activeClients", activeClients
        );
    }

    private PageResponse<ClientResponse> buildPageResponse(Page<Client> page) {
        List<ClientResponse> content = clientMapper.toResponseList(page.getContent());

        return PageResponse.<ClientResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
