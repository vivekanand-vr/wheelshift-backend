package com.wheelshiftpro.dto.request;

import com.wheelshiftpro.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateRequest {
    
    @NotBlank(message = "Template name is required")
    private String name;
    
    @NotNull(message = "Channel is required")
    private NotificationChannel channel;
    
    @Builder.Default
    private String locale = "en";
    
    private String subject;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private List<String> variables;
}
