package com.supplierdata.platform.transform;

import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import java.io.Reader;
import java.util.List;

/**
 * Strategy interface for supplier-specific payload normalization.
 * Enables the platform to scale across 60+ suppliers with distinct data structures
 * across JSON, CSV, XML, and Text formats.
 */
public interface SupplierNormalizer {

    /**
     * Returns the supplier code this normalizer target, or "DEFAULT" for generic format handling.
     */
    String getSupplierCode();

    /**
     * Returns the supported format: JSON, CSV, XML, or TEXT.
     */
    String getSupportedFormat();

    /**
     * Normalizes the raw payload reader stream into company-standardized internal records.
     */
    List<NormalizedSupplierRecord> normalize(Reader reader) throws Exception;
}
