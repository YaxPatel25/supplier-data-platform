package com.supplierdata.platform.service;

import com.supplierdata.platform.domain.entity.SupplierRecord;
import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import com.supplierdata.platform.repository.SupplierRecordRepository;
import com.supplierdata.platform.transform.CsvNormalizer;
import com.supplierdata.platform.transform.JsonSchemaMapper;
import com.supplierdata.platform.transform.TextFlatFileParser;
import com.supplierdata.platform.transform.XmlXsltTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

/**
 * Synchronous ingestion path for suppliers submitting data directly via the
 * REST API (as opposed to the dropzone batch job). Routes the payload to
 * the correct normalizer based on declared format, persists the result, and
 * logs failures the way production support originally monitored FlatFile
 * processing failures via email -- here via structured logging that a real
 * deployment would wire to an alerting channel.
 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final CsvNormalizer csvNormalizer;
    private final XmlXsltTransformer xmlTransformer;
    private final JsonSchemaMapper jsonSchemaMapper;
    private final TextFlatFileParser textFlatFileParser;
    private final SupplierRecordRepository supplierRecordRepository;

    public IngestionService(CsvNormalizer csvNormalizer,
                             XmlXsltTransformer xmlTransformer,
                             JsonSchemaMapper jsonSchemaMapper,
                             TextFlatFileParser textFlatFileParser,
                             SupplierRecordRepository supplierRecordRepository) {
        this.csvNormalizer = csvNormalizer;
        this.xmlTransformer = xmlTransformer;
        this.jsonSchemaMapper = jsonSchemaMapper;
        this.textFlatFileParser = textFlatFileParser;
        this.supplierRecordRepository = supplierRecordRepository;
    }

    public List<SupplierRecord> ingest(String payload, String format) {
        try {
            Reader reader = new StringReader(payload);
            List<NormalizedSupplierRecord> normalized = switch (format.toUpperCase()) {
                case "CSV" -> csvNormalizer.normalize(reader);
                case "XML" -> xmlTransformer.normalize(reader);
                case "JSON" -> jsonSchemaMapper.normalize(reader);
                case "TEXT" -> textFlatFileParser.normalize(reader);
                default -> throw new IllegalArgumentException("Unsupported supplier format: " + format);
            };

            List<SupplierRecord> saved = normalized.stream()
                    .map(this::toEntity)
                    .map(supplierRecordRepository::save)
                    .toList();

            log.info("Ingested {} records from {} payload", saved.size(), format);
            return saved;

        } catch (Exception ex) {
            // Equivalent of the original "monitored production support emails to
            // identify FlatFile processing failures" workflow -- logged here for
            // an alerting pipeline (e.g. ELK, Datadog) to pick up.
            log.error("Supplier ingestion failed for format {}: {}", format, ex.getMessage(), ex);
            SupplierRecord failed = new SupplierRecord();
            failed.setSourceFormat(format);
            failed.setSupplierCode("UNKNOWN");
            failed.setStatus("FAILED");
            failed.setFailureReason(ex.getMessage());
            failed.setRawPayloadReference("inline-submission");
            failed.setCruiseLine("UNKNOWN");
            failed.setShipName("UNKNOWN");
            failed.setSailingDate("UNKNOWN");
            failed.setFareCode("UNKNOWN");
            supplierRecordRepository.save(failed);
            throw new RuntimeException("Ingestion failed: " + ex.getMessage(), ex);
        }
    }

    private SupplierRecord toEntity(NormalizedSupplierRecord dto) {
        SupplierRecord record = new SupplierRecord();
        record.setSupplierCode(dto.getSupplierCode());
        record.setSourceFormat(dto.getSourceFormat());
        record.setCruiseLine(dto.getCruiseLine());
        record.setShipName(dto.getShipName());
        record.setSailingDate(dto.getSailingDate());
        record.setFareCode(dto.getFareCode());
        record.setRawPayloadReference("inline-submission");
        record.setStatus("NORMALIZED");
        return record;
    }
}
