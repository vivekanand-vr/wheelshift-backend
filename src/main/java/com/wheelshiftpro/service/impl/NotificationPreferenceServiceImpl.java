package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.NotificationPreferenceRequest;
import com.wheelshiftpro.dto.response.NotificationPreferenceResponse;
import com.wheelshiftpro.entity.NotificationPreference;
import com.wheelshiftpro.enums.PrincipalType;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.repository.NotificationPreferenceRepository;
import com.wheelshiftpro.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {
    
    private final NotificationPreferenceRepository preferenceRepository;
    
    @Override
    @Transactional
    public NotificationPreferenceResponse createOrUpdatePreference(NotificationPreferenceRequest request) {
        log.info("Creating/Updating notification preference: principalType={}, principalId={}, eventType={}, channel={}", 
                request.getPrincipalType(), request.getPrincipalId(), request.getEventType(), request.getChannel());
        
        NotificationPreference preference = preferenceRepository
                .findByPrincipalTypeAndPrincipalIdAndEventTypeAndChannel(
                        request.getPrincipalType(),
                        request.getPrincipalId(),
                        request.getEventType(),
                        request.getChannel())
                .orElse(NotificationPreference.builder()
                        .principalType(request.getPrincipalType())
                        .principalId(request.getPrincipalId())
                        .eventType(request.getEventType())
                        .channel(request.getChannel())
                        .createdAt(LocalDateTime.now())
                        .build());
        
        preference.setEnabled(request.getEnabled());
        preference.setFrequency(request.getFrequency());
        preference.setQuietHoursStart(request.getQuietHoursStart());
        preference.setQuietHoursEnd(request.getQuietHoursEnd());
        preference.setSeverityThreshold(request.getSeverityThreshold());
        preference.setUpdatedAt(LocalDateTime.now());
        
        preference = preferenceRepository.save(preference);
        
        return mapToResponse(preference);
    }
    
    @Override
    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getPreferenceById(Long id) {
        NotificationPreference preference = preferenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preference not found with id: " + id));
        
        return mapToResponse(preference);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationPreferenceResponse> getPreferencesForPrincipal(
            PrincipalType principalType, Long principalId) {
        
        List<NotificationPreference> preferences = preferenceRepository
                .findByPrincipalTypeAndPrincipalId(principalType, principalId);
        
        return preferences.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationPreferenceResponse> getAllPreferences(Pageable pageable) {
        return preferenceRepository.findAllByOrderByPrincipalTypeAsc(pageable)
                .map(this::mapToResponse);
    }
    
    @Override
    @Transactional
    public void deletePreference(Long id) {
        if (!preferenceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Preference not found with id: " + id);
        }
        
        preferenceRepository.deleteById(id);
        log.info("Deleted notification preference with id: {}", id);
    }
    
    private NotificationPreferenceResponse mapToResponse(NotificationPreference preference) {
        return NotificationPreferenceResponse.builder()
                .id(preference.getId())
                .principalType(preference.getPrincipalType())
                .principalId(preference.getPrincipalId())
                .eventType(preference.getEventType())
                .channel(preference.getChannel())
                .enabled(preference.getEnabled())
                .frequency(preference.getFrequency())
                .quietHoursStart(preference.getQuietHoursStart())
                .quietHoursEnd(preference.getQuietHoursEnd())
                .severityThreshold(preference.getSeverityThreshold())
                .createdAt(preference.getCreatedAt())
                .updatedAt(preference.getUpdatedAt())
                .build();
    }
}
