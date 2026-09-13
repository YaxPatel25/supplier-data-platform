package com.supplierdata.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    /**
     * WebClient used for consuming live supplier APIs (CruiseCache-style
     * real-time cruise data), wrapped with Resilience4j retry/circuit-breaker
     * in CruiseCacheClient.
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
