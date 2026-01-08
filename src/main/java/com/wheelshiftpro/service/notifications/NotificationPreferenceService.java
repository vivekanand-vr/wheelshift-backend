package com.wheelshiftpro.service.notifications;

import com.wheelshiftpro.dto.request.notifications.NotificationPreferenceRequest;
import com.wheelshiftpro.dto.response.notifications.NotificationPreferenceResponse;
import com.wheelshiftpro.enums.PrincipalType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationPreferenceService {
    
    NotificationPreferenceResponse createOrUpdatePreference(NotificationPreferenceRequest request);
    
    NotificationPreferenceResponse getPreferenceById(Long id);
    
    List<NotificationPreferenceResponse> getPreferencesForPrincipal(PrincipalType principalType, Long principalId);
    
    Page<NotificationPreferenceResponse> getAllPreferences(Pageable pageable);
    
    void deletePreference(Long id);
}
