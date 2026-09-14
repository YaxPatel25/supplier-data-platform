package com.supplierdata.platform.transform;

import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry managing supplier normalization strategies.
 * Routes incoming payloads to registered supplier strategies (by supplierCode + format),
 * or falls back to default format normalizers.
 */
@Component
public class SupplierNormalizationRegistry {

    private static final Logger log = LoggerFactory.getLogger(SupplierNormalizationRegistry.class);

    private final Map<String, SupplierNormalizer> normalizers = new HashMap<>();

    public SupplierNormalizationRegistry(List<SupplierNormalizer> supplierNormalizers) {
        for (SupplierNormalizer normalizer : supplierNormalizers) {
            String key = buildKey(normalizer.getSupplierCode(), normalizer.getSupportedFormat());
            normalizers.put(key, normalizer);
            log.info("Registered supplier normalizer key: {}", key);
        }
    }

    public List<NormalizedSupplierRecord> normalize(String supplierCode, String format, Reader reader) throws Exception {
        if (format == null || format.isBlank()) {
            throw new IllegalArgumentException("Format must be specified");
        }

        String fmtUpper = format.trim().toUpperCase();
        String suppUpper = (supplierCode != null && !supplierCode.isBlank()) ? supplierCode.trim().toUpperCase() : "DEFAULT";

        String specificKey = buildKey(suppUpper, fmtUpper);
        SupplierNormalizer normalizer = normalizers.get(specificKey);

        if (normalizer == null) {
            String defaultKey = buildKey("DEFAULT", fmtUpper);
            normalizer = normalizers.get(defaultKey);
        }

        if (normalizer == null) {
            throw new IllegalArgumentException("No normalizer registered for format: " + format + " (Supplier: " + supplierCode + ")");
        }

        log.info("Normalizing payload using strategy for supplier '{}' and format '{}'", normalizer.getSupplierCode(), normalizer.getSupportedFormat());
        return normalizer.normalize(reader);
    }

    private String buildKey(String supplierCode, String format) {
        return supplierCode.toUpperCase() + ":" + format.toUpperCase();
    }
}
