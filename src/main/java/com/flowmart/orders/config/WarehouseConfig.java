package com.flowmart.orders.config;

import com.flowmart.orders.service.legacy.WarehouseClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WarehouseConfig {

    @Bean
    public WarehouseClient warehouseClient() {
        return new WarehouseClient();
    }
}
