package dev.kafka.payment.messaging;

import dev.kafka.contracts.PaymentResult;
import dev.kafka.contracts.Topics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {
    private final KafkaTemplate<String, Object> kafka;
    public PaymentEventPublisher(KafkaTemplate<String, Object> kafka) { this.kafka = kafka; }
    public void publish(PaymentResult result) throws Exception {
        kafka.send(Topics.PAYMENT_RESULT, result.orderId().toString(), result).get();
    }
}
