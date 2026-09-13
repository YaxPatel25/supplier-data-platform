package com.supplierdata.platform.client;

import com.supplierdata.platform.domain.entity.CruiseData;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Consumes a live supplier API for real-time cruise information, equivalent
 * to the original "CruiseCache integrations by consuming live supplier
 * APIs" work. Wrapped with retry + circuit-breaker so a flaky supplier
 * endpoint degrades gracefully instead of cascading failures.
 */
@Component
public class CruiseCacheClient {

    private final WebClient webClient;

    public CruiseCacheClient(WebClient.Builder builder,
                              @Value("${cruisecache.base-url:https://example-supplier.invalid}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Retry(name = "cruiseCacheApi")
    @CircuitBreaker(name = "cruiseCacheApi", fallbackMethod = "fallbackAvailability")
    public Mono<CruiseData> fetchAvailability(String shipName, String sailingDate) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/availability")
                        .queryParam("ship", shipName)
                        .queryParam("sailingDate", sailingDate)
                        .build())
                .retrieve()
                .bodyToMono(CruiseData.class);
    }

    @SuppressWarnings("unused")
    private Mono<CruiseData> fallbackAvailability(String shipName, String sailingDate, Throwable throwable) {
        CruiseData fallback = new CruiseData();
        fallback.setShipName(shipName);
        fallback.setSailingDate(sailingDate);
        fallback.setAvailableCabins(-1); // sentinel: supplier temporarily unavailable
        fallback.setSupplierCode("UNAVAILABLE");
        return Mono.just(fallback);
    }
}
