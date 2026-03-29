package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.ClientRequest;
import com.wheelshiftpro.dto.response.ClientResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.ClientStatus;

import java.util.List;

/**
 * Service interface for Client operations.
 * Manages customer information and relationships.
 */
public interface ClientService {

    /**
     * Creates a new client.
     * Validates email uniqueness.
     *
     * @param request the client data
     * @return created client response
     * @throws com.wheelshiftpro.exception.DuplicateResourceException if email exists
     */
    ClientResponse createClient(ClientRequest request);

    /**
     * Updates an existing client.
     *
     * @param id the client ID
     * @param request updated client data
     * @return updated client response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if client not found
     */
    ClientResponse updateClient(Long id, ClientRequest request);

    /**
     * Retrieves a client by ID.
     *
     * @param id the client ID
     * @return client response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if client not found
     */
    ClientResponse getClientById(Long id);

    /**
     * Retrieves a client by email.
     *
     * @param email the client email
     * @return client response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if client not found
     */
    ClientResponse getClientByEmail(String email);

    /**
     * Retrieves all clients with pagination.
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated client responses
     */
    PageResponse<ClientResponse> getAllClients(int page, int size);

    /**
     * Searches clients by name, email, or phone.
     *
     * @param searchTerm search term
     * @param page page number
     * @param size page size
     * @return paginated search results
     */
    PageResponse<ClientResponse> searchClients(String searchTerm, int page, int size);

    /**
     * Retrieves clients by status.
     *
     * @param status the client status
     * @param page page number
     * @param size page size
     * @return paginated client responses
     */
    PageResponse<ClientResponse> getClientsByStatus(ClientStatus status, int page, int size);

    /**
     * Deletes a client.
     * Performs soft delete.
     *
     * @param id the client ID
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if client not found
     */
    void deleteClient(Long id);

    /**
     * Retrieves top buyers by total purchases.
     *
     * @param limit number of results
     * @return list of top clients
     */
    List<ClientResponse> getTopBuyers(int limit);

    /**
     * Retrieves client statistics by status.
     *
     * @return statistics map
     */
    Object getClientStatistics();

    /**
     * Updates client purchase information after a sale.
     * Called internally after successful sale.
     *
     * @param clientId the client ID
     */
    void incrementPurchaseCount(Long clientId);

    /**
     * Updates the status of a client.
     *
     * @param id     the client ID
     * @param status the new status
     * @return updated client response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if client not found
     */
    ClientResponse updateClientStatus(Long id, ClientStatus status);
}
