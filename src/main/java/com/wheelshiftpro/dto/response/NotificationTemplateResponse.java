package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateResponse {
    
    private Long id;
    private String name;
    private NotificationChannel channel;
    private String locale;
    private Integer version;
    private String subject;
    private String content;
    private List<String> variables;
    private Long createdByEmployeeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
