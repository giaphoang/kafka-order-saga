package dev.kafka.orders.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kafka.contracts.OrderCreated;
import dev.kafka.orders.repository.OutboxRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublisher {
    private final OutboxRepository repository;
    private final KafkaTemplate<String, Object> kafka;
    private final ObjectMapper json;

    public OutboxPublisher(OutboxRepository repository, KafkaTemplate<String, Object> kafka, ObjectMapper json) {
        this.repository = repository;
        this.kafka = kafka;
        this.json = json;
    }

    @Scheduled(fixedDelayString = "${outbox.poll-ms:500}")
    @Transactional
    public void publish() throws Exception {
        for (var row : repository.findTop50ByPublishedAtIsNullOrderByCreatedAt()) {
            OrderCreated event = json.readValue(row.getPayload(), OrderCreated.class);
            kafka.send(row.getTopic(), row.getAggregateId().toString(), event).get();
            row.markPublished();
        }
    }
}
