package com.supplierdata.platform.transform;

import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultTextNormalizer implements SupplierNormalizer {

    private static final String DELIMITER = "\\|";

    @Override
    public String getSupplierCode() {
        return "DEFAULT";
    }

    @Override
    public String getSupportedFormat() {
        return "TEXT";
    }

    @Override
    public List<NormalizedSupplierRecord> normalize(Reader textReader) throws Exception {
        List<NormalizedSupplierRecord> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(textReader)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue;
                String[] parts = line.split(DELIMITER);
                if (parts.length != 5) {
                    throw new IllegalArgumentException(
                            "Malformed FlatFile line " + lineNumber + ": expected 5 pipe-delimited fields, got " + parts.length);
                }
                results.add(new NormalizedSupplierRecord(
                        parts[0].trim(), "TEXT", parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim()));
            }
        }
        return results;
    }
}
