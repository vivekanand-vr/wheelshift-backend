package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.NotificationTemplateRequest;
import com.wheelshiftpro.dto.response.NotificationTemplateResponse;
import com.wheelshiftpro.enums.NotificationChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface NotificationTemplateService {
    
    NotificationTemplateResponse createTemplate(NotificationTemplateRequest request, Long createdByEmployeeId);
    
    NotificationTemplateResponse getTemplateById(Long id);
    
    NotificationTemplateResponse getLatestTemplate(String name, NotificationChannel channel, String locale);
    
    Page<NotificationTemplateResponse> getAllTemplates(Pageable pageable);
    
    Page<NotificationTemplateResponse> getTemplatesByChannel(NotificationChannel channel, Pageable pageable);
    
    NotificationTemplateResponse updateTemplate(Long id, NotificationTemplateRequest request);
    
    void deleteTemplate(Long id);
    
    String renderTemplate(String templateContent, Map<String, Object> variables);
}
