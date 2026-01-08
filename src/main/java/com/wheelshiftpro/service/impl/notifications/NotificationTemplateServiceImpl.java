package com.wheelshiftpro.service.impl.notifications;

import com.wheelshiftpro.dto.request.notifications.NotificationTemplateRequest;
import com.wheelshiftpro.dto.response.notifications.NotificationTemplateResponse;
import com.wheelshiftpro.entity.notifications.NotificationTemplate;
import com.wheelshiftpro.enums.notifications.NotificationChannel;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.repository.notifications.NotificationTemplateRepository;
import com.wheelshiftpro.service.notifications.NotificationTemplateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationTemplateServiceImpl implements NotificationTemplateService {
    
    private final NotificationTemplateRepository templateRepository;
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    
    @Override
    @Transactional
    public NotificationTemplateResponse createTemplate(NotificationTemplateRequest request, Long createdByEmployeeId) {
        log.info("Creating notification template: name={}, channel={}", request.getName(), request.getChannel());
        
        // Get the latest version for this template
        Integer latestVersion = templateRepository
                .findLatestByNameAndChannelAndLocale(request.getName(), request.getChannel(), request.getLocale())
                .map(NotificationTemplate::getVersion)
                .orElse(0);
        
        NotificationTemplate template = NotificationTemplate.builder()
                .name(request.getName())
                .channel(request.getChannel())
                .locale(request.getLocale())
                .version(latestVersion + 1)
                .subject(request.getSubject())
                .content(request.getContent())
                .variables(request.getVariables())
                .createdByEmployeeId(createdByEmployeeId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        template = templateRepository.save(template);
        
        return mapToResponse(template);
    }
    
    @Override
    @Transactional(readOnly = true)
    public NotificationTemplateResponse getTemplateById(Long id) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
        
        return mapToResponse(template);
    }
    
    @Override
    @Transactional(readOnly = true)
    public NotificationTemplateResponse getLatestTemplate(String name, NotificationChannel channel, String locale) {
        NotificationTemplate template = templateRepository
                .findLatestByNameAndChannelAndLocale(name, channel, locale)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Template not found: name=" + name + ", channel=" + channel + ", locale=" + locale));
        
        return mapToResponse(template);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationTemplateResponse> getAllTemplates(Pageable pageable) {
        return templateRepository.findAllByOrderByNameAsc(pageable)
                .map(this::mapToResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationTemplateResponse> getTemplatesByChannel(NotificationChannel channel, Pageable pageable) {
        return templateRepository.findByChannelOrderByNameAsc(channel, pageable)
                .map(this::mapToResponse);
    }
    
    @Override
    @Transactional
    public NotificationTemplateResponse updateTemplate(Long id, NotificationTemplateRequest request) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
        
        template.setSubject(request.getSubject());
        template.setContent(request.getContent());
        template.setVariables(request.getVariables());
        template.setUpdatedAt(LocalDateTime.now());
        
        template = templateRepository.save(template);
        
        return mapToResponse(template);
    }
    
    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Template not found with id: " + id);
        }
        
        templateRepository.deleteById(id);
        log.info("Deleted notification template with id: {}", id);
    }
    
    @Override
    public String renderTemplate(String templateContent, Map<String, Object> variables) {
        if (templateContent == null || variables == null) {
            return templateContent;
        }
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(templateContent);
        
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            Object value = variables.get(variableName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    private NotificationTemplateResponse mapToResponse(NotificationTemplate template) {
        return NotificationTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .channel(template.getChannel())
                .locale(template.getLocale())
                .version(template.getVersion())
                .subject(template.getSubject())
                .content(template.getContent())
                .variables(template.getVariables())
                .createdByEmployeeId(template.getCreatedByEmployeeId())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
