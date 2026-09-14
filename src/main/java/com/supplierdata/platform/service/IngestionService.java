package com.supplierdata.platform.service;

import com.supplierdata.platform.domain.entity.SupplierRecord;
import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import com.supplierdata.platform.repository.SupplierRecordRepository;
import com.supplierdata.platform.transform.SupplierNormalizationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

/**
 * Unified ingestion service for suppliers submitting data across REST APIs,
 * Azure Data Lake Storage, FTP/SFTP servers, and Batch Dropzones.
 * Normalizes multi-supplier formats using SupplierNormalizationRegistry.
 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final SupplierNormalizationRegistry normalizationRegistry;
    private final SupplierRecordRepository supplierRecordRepository;
    private final AzureDataLakeStorageService azureDataLakeStorageService;
    private final FtpStorageService ftpStorageService;

    public IngestionService(SupplierNormalizationRegistry normalizationRegistry,
                            SupplierRecordRepository supplierRecordRepository,
                            AzureDataLakeStorageService azureDataLakeStorageService,
                            FtpStorageService ftpStorageService) {
        this.normalizationRegistry = normalizationRegistry;
        this.supplierRecordRepository = supplierRecordRepository;
        this.azureDataLakeStorageService = azureDataLakeStorageService;
        this.ftpStorageService = ftpStorageService;
    }

    /**
     * Backward-compatible REST ingestion endpoint method.
     */
    public List<SupplierRecord> ingest(String payload, String format) {
        return ingest("DEFAULT", format, "REST_API", payload, "inline-submission");
    }

    /**
     * Comprehensive multi-supplier ingestion method supporting custom supplier code and source type.
     */
    public List<SupplierRecord> ingest(String supplierCode, String format, String sourceType, String payload, String rawPayloadRef) {
        try {
            Reader reader = new StringReader(payload);
            List<NormalizedSupplierRecord> normalized = normalizationRegistry.normalize(supplierCode, format, reader);

            List<SupplierRecord> saved = normalized.stream()
                    .map(dto -> toEntity(dto, sourceType, rawPayloadRef))
                    .map(supplierRecordRepository::save)
                    .toList();

            log.info("Ingested {} records for supplier '{}' from format '{}' via source '{}'",
                    saved.size(), supplierCode, format, sourceType);
            return saved;

        } catch (Exception ex) {
            log.error("Supplier ingestion failed for supplier '{}', format '{}', source '{}': {}",
                    supplierCode, format, sourceType, ex.getMessage(), ex);
            SupplierRecord failed = new SupplierRecord();
            failed.setSupplierCode(supplierCode != null ? supplierCode : "UNKNOWN");
            failed.setSourceFormat(format != null ? format : "UNKNOWN");
            failed.setSourceType(sourceType != null ? sourceType : "REST_API");
            failed.setStatus("FAILED");
            failed.setFailureReason(ex.getMessage());
            failed.setRawPayloadReference(rawPayloadRef != null ? rawPayloadRef : "inline-submission");
            failed.setCruiseLine("UNKNOWN");
            failed.setShipName("UNKNOWN");
            failed.setSailingDate("UNKNOWN");
            failed.setFareCode("UNKNOWN");
            supplierRecordRepository.save(failed);
            throw new RuntimeException("Ingestion failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Pulls payload file directly from Azure Data Lake Storage container and ingests records.
     */
    public List<SupplierRecord> ingestFromAzureDataLake(String supplierCode, String format, String containerName, String filePath) {
        try {
            String payload = azureDataLakeStorageService.readContent(containerName, filePath);
            String rawRef = "adls://" + containerName + "/" + filePath;
            return ingest(supplierCode, format, "AZURE_DATA_LAKE", payload, rawRef);
        } catch (Exception e) {
            log.error("Failed to ingest from Azure Data Lake (container: {}, path: {}): {}", containerName, filePath, e.getMessage());
            throw new RuntimeException("Azure Data Lake ingestion failed: " + e.getMessage(), e);
        }
    }

    /**
     * Pulls payload file directly from FTP/SFTP server path and ingests records.
     */
    public List<SupplierRecord> ingestFromFtp(String supplierCode, String format, String remotePath) {
        try {
            String payload = ftpStorageService.downloadFile(remotePath);
            String rawRef = "ftp://" + remotePath;
            return ingest(supplierCode, format, "FTP", payload, rawRef);
        } catch (Exception e) {
            log.error("Failed to ingest from FTP path {}: {}", remotePath, e.getMessage());
            throw new RuntimeException("FTP ingestion failed: " + e.getMessage(), e);
        }
    }

    private SupplierRecord toEntity(NormalizedSupplierRecord dto, String sourceType, String rawPayloadRef) {
        SupplierRecord record = new SupplierRecord();
        record.setSupplierCode(dto.getSupplierCode());
        record.setSourceFormat(dto.getSourceFormat());
        record.setSourceType(sourceType != null ? sourceType : "REST_API");
        record.setCruiseLine(dto.getCruiseLine());
        record.setShipName(dto.getShipName());
        record.setSailingDate(dto.getSailingDate());
        record.setFareCode(dto.getFareCode());
        record.setRawPayloadReference(rawPayloadRef != null ? rawPayloadRef : "inline-submission");
        record.setStatus("NORMALIZED");
        return record;
    }
}
