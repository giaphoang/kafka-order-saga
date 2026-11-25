package dev.kafka.notification.service;

import dev.kafka.contracts.OrderStatusChanged;
import org.slf4j.*;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendOrderEmail(OrderStatusChanged event) {
        log.info("EMAIL to={} order={} status={} detail={}",
                event.customerEmail(), event.orderId(), event.status(), event.detail());
    }

    public void recordDeadLetter(OrderStatusChanged event) {
        log.error("Manual intervention required for notification event {}", event.eventId());
    }
}
