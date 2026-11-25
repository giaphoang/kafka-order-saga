package dev.kafka.inventory.config;

import dev.kafka.inventory.model.StockItem;
import dev.kafka.inventory.repository.StockRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import java.util.List;

@Configuration
public class StockDataConfig {
    @Bean CommandLineRunner seedStock(StockRepository repository) {
        return args -> {
            if (repository.count() == 0) repository.saveAll(List.of(
                    new StockItem("LAPTOP-14", 20),
                    new StockItem("HEADSET-PRO", 50),
                    new StockItem("USB-C-DOCK", 30)));
        };
    }
}
