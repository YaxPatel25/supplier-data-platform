package com.supplierdata.platform.controller;

import com.supplierdata.platform.domain.entity.SupplierRecord;
import com.supplierdata.platform.repository.SupplierRecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-side API for reviewing ingested supplier records -- the equivalent
 * of the internal tooling used for "production support to ensure reliable
 * supplier data processing", letting support staff check status/failures
 * without querying SQL Server directly.
 */
@RestController
@RequestMapping("/api/v1/supplier-records")
public class SupplierIngestController {

    private final SupplierRecordRepository repository;

    public SupplierIngestController(SupplierRecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<SupplierRecord> all() {
        return repository.findAll();
    }

    @GetMapping("/status/{status}")
    public List<SupplierRecord> byStatus(@PathVariable String status) {
        return repository.findByStatus(status.toUpperCase());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierRecord> byId(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
