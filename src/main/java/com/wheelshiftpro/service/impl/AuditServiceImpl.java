package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.response.AuditLogResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.entity.AuditLog;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.repository.AuditLogRepository;
import com.wheelshiftpro.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void log(AuditCategory category, Long entityId, String action, AuditLevel level,
                    Employee performedBy, String details) {
        AuditLog auditLog = AuditLog.builder()
                .category(category)
                .entityId(entityId)
                .action(action)
                .level(level)
                .performedBy(performedBy)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);

        log.debug("Audit [{}][{}] {} entity={} by employee={}",
                level, category, action, entityId,
                performedBy != null ? performedBy.getId() : "system");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogs(AuditCategory category, AuditLevel level,
                                                       String action, Long entityId,
                                                       Long performedById, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<AuditLog> spec = buildSpec(category, level, action, entityId, performedById);
        Page<AuditLog> resultsPage = auditLogRepository.findAll(spec, pageable);

        List<AuditLogResponse> content = resultsPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.<AuditLogResponse>builder()
                .content(content)
                .pageNumber(resultsPage.getNumber())
                .pageSize(resultsPage.getSize())
                .totalElements(resultsPage.getTotalElements())
                .totalPages(resultsPage.getTotalPages())
                .last(resultsPage.isLast())
                .first(resultsPage.isFirst())
                .build();
    }

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    private Specification<AuditLog> buildSpec(AuditCategory category, AuditLevel level,
                                               String action, Long entityId, Long performedById) {
        List<Specification<AuditLog>> specs = new ArrayList<>();

        if (category != null) {
            specs.add((root, q, cb) -> cb.equal(root.get("category"), category));
        }
        if (level != null) {
            specs.add((root, q, cb) -> cb.equal(root.get("level"), level));
        }
        if (action != null && !action.isBlank()) {
            specs.add((root, q, cb) -> cb.equal(cb.upper(root.get("action")), action.toUpperCase()));
        }
        if (entityId != null) {
            specs.add((root, q, cb) -> cb.equal(root.get("entityId"), entityId));
        }
        if (performedById != null) {
            specs.add((root, q, cb) -> cb.equal(root.get("performedBy").get("id"), performedById));
        }

        return specs.stream()
                .reduce(Specification::and)
                .orElse((root, query, cb) -> cb.conjunction());
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .category(log.getCategory())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .level(log.getLevel())
                .performedById(log.getPerformedBy() != null ? log.getPerformedBy().getId() : null)
                .performedByName(log.getPerformedBy() != null ? log.getPerformedBy().getName() : null)
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
