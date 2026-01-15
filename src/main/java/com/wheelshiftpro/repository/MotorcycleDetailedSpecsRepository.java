package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.MotorcycleDetailedSpecs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for MotorcycleDetailedSpecs entity.
 * Provides data access methods for motorcycle detailed specifications.
 */
@Repository
public interface MotorcycleDetailedSpecsRepository extends JpaRepository<MotorcycleDetailedSpecs, Long> {

    /**
     * Find detailed specs by motorcycle ID
     */
    Optional<MotorcycleDetailedSpecs> findByMotorcycleId(Long motorcycleId);

    /**
     * Delete detailed specs by motorcycle ID
     */
    void deleteByMotorcycleId(Long motorcycleId);

    /**
     * Check if detailed specs exist for a motorcycle
     */
    boolean existsByMotorcycleId(Long motorcycleId);
}
