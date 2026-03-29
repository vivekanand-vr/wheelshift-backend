package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.Client;
import com.wheelshiftpro.enums.ClientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Client entity.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Find client by email.
     */
    Optional<Client> findByEmail(String email);

    /**
     * Check if client exists by email.
     */
    boolean existsByEmail(String email);

    /**
     * Check if another client exists with the same email, excluding the given ID.
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Find clients by status.
     */
    Page<Client> findByStatus(ClientStatus status, Pageable pageable);

    /**
     * Find clients by location.
     */
    List<Client> findByLocation(String location);

    /**
     * Search clients by name, email, or phone.
     */
    @Query("SELECT c FROM Client c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "c.phone LIKE CONCAT('%', :searchTerm, '%')")
    Page<Client> searchClients(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find top buyers by total purchases.
     */
    @Query("SELECT c FROM Client c WHERE c.totalPurchases > 0 ORDER BY c.totalPurchases DESC")
    List<Client> findTopBuyers(Pageable pageable);

    /**
     * Get client count by status.
     */
    @Query("SELECT c.status as status, COUNT(c) as count FROM Client c GROUP BY c.status")
    List<Object[]> getClientCountByStatus();

    /**
     * Get client count by location.
     */
    @Query("SELECT c.location as location, COUNT(c) as count FROM Client c GROUP BY c.location")
    List<Object[]> getClientCountByLocation();
}
