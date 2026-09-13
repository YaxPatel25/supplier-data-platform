package com.supplierdata.platform.controller;

import com.supplierdata.platform.client.CruiseCacheClient;
import com.supplierdata.platform.domain.entity.CruiseData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Exposes live supplier availability lookups (CruiseCache-style), backed by
 * CruiseCacheClient's resilient WebClient calls.
 */
@RestController
public class CruiseCacheController {

    private final CruiseCacheClient cruiseCacheClient;

    public CruiseCacheController(CruiseCacheClient cruiseCacheClient) {
        this.cruiseCacheClient = cruiseCacheClient;
    }

    @GetMapping("/api/v1/cruise-cache/availability")
    public Mono<CruiseData> availability(@RequestParam String shipName, @RequestParam String sailingDate) {
        return cruiseCacheClient.fetchAvailability(shipName, sailingDate);
    }
}
