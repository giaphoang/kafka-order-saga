package dev.kafka.inventory.messaging;

import dev.kafka.contracts.InventoryResult;
import dev.kafka.contracts.Topics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {
    private final KafkaTemplate<String, Object> kafka;
    public InventoryEventPublisher(KafkaTemplate<String, Object> kafka) { this.kafka = kafka; }
    public void publish(InventoryResult result) throws Exception {
        kafka.send(Topics.INVENTORY_RESULT, result.orderId().toString(), result).get();
    }
}
