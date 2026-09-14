package com.supplierdata.platform.controller;

import com.supplierdata.platform.domain.entity.SupplierRecord;
import com.supplierdata.platform.service.IngestionService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for suppliers submitting data through direct API integrations,
 * Azure Data Lake Storage, and FTP/SFTP remote sources.
 */
@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierApiController {

    private final IngestionService ingestionService;

    public SupplierApiController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    public record SupplierSubmission(
            String supplierCode,
            @NotBlank String format,
            String sourceType,
            @NotBlank String payload
    ) {}

    public record AzureDataLakeIngestRequest(
            String supplierCode,
            @NotBlank String format,
            @NotBlank String containerName,
            @NotBlank String filePath
    ) {}

    public record FtpIngestRequest(
            String supplierCode,
            @NotBlank String format,
            @NotBlank String remotePath
    ) {}

    @PostMapping("/submissions")
    public ResponseEntity<List<SupplierRecord>> submit(@RequestBody SupplierSubmission submission) {
        String suppCode = (submission.supplierCode() != null && !submission.supplierCode().isBlank())
                ? submission.supplierCode() : "DEFAULT";
        String srcType = (submission.sourceType() != null && !submission.sourceType().isBlank())
                ? submission.sourceType() : "REST_API";

        List<SupplierRecord> saved = ingestionService.ingest(suppCode, submission.format(), srcType, submission.payload(), "api-submission");
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/ingest/azure")
    public ResponseEntity<List<SupplierRecord>> ingestAzure(@RequestBody AzureDataLakeIngestRequest request) {
        String suppCode = (request.supplierCode() != null && !request.supplierCode().isBlank())
                ? request.supplierCode() : "DEFAULT";

        List<SupplierRecord> saved = ingestionService.ingestFromAzureDataLake(
                suppCode, request.format(), request.containerName(), request.filePath());
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/ingest/ftp")
    public ResponseEntity<List<SupplierRecord>> ingestFtp(@RequestBody FtpIngestRequest request) {
        String suppCode = (request.supplierCode() != null && !request.supplierCode().isBlank())
                ? request.supplierCode() : "DEFAULT";

        List<SupplierRecord> saved = ingestionService.ingestFromFtp(
                suppCode, request.format(), request.remotePath());
        return ResponseEntity.ok(saved);
    }
}
