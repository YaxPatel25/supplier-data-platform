package com.supplierdata.platform.controller;

import com.supplierdata.platform.domain.entity.SupplierRecord;
import com.supplierdata.platform.service.IngestionService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for suppliers submitting data through direct API integrations --
 * equivalent to "Developed and supported RESTful Web APIs for suppliers
 * submitting data through API integrations" in the original role. Accepts
 * a raw payload plus its declared format (CSV/XML/JSON/TEXT).
 */
@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierApiController {

    private final IngestionService ingestionService;

    public SupplierApiController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    public record SupplierSubmission(@NotBlank String format, @NotBlank String payload) {}

    @PostMapping("/submissions")
    public ResponseEntity<List<SupplierRecord>> submit(@RequestBody SupplierSubmission submission) {
        List<SupplierRecord> saved = ingestionService.ingest(submission.payload(), submission.format());
        return ResponseEntity.ok(saved);
    }
}
