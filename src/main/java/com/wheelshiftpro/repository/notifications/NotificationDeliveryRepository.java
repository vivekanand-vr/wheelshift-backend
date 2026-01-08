package com.wheelshiftpro.repository.notifications;

import com.wheelshiftpro.entity.notifications.NotificationDelivery;
import com.wheelshiftpro.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long> {
    
    List<NotificationDelivery> findByJobId(Long jobId);
    
    List<NotificationDelivery> findByStatus(DeliveryStatus status);
}
