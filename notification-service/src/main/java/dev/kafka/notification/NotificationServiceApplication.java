package dev.kafka.notification;
import org.springframework.boot.SpringApplication; import org.springframework.boot.autoconfigure.SpringBootApplication; import org.springframework.kafka.annotation.EnableKafkaRetryTopic; import org.springframework.scheduling.annotation.EnableScheduling;
@EnableScheduling @EnableKafkaRetryTopic @SpringBootApplication public class NotificationServiceApplication { public static void main(String[] a){SpringApplication.run(NotificationServiceApplication.class,a);} }
