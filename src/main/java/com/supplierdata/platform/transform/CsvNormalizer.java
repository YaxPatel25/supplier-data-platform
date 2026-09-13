package com.supplierdata.platform.transform;

import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Normalizes supplier CSV feeds into the internal schema. Expects a header
 * row: supplierCode,cruiseLine,shipName,sailingDate,fareCode
 */
@Component
public class CsvNormalizer {

    public List<NormalizedSupplierRecord> normalize(Reader csvReader) throws IOException {
        List<NormalizedSupplierRecord> results = new ArrayList<>();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build();

        try (CSVParser parser = new CSVParser(csvReader, format)) {
            for (CSVRecord record : parser) {
                NormalizedSupplierRecord normalized = new NormalizedSupplierRecord(
                        record.get("supplierCode"),
                        "CSV",
                        record.get("cruiseLine"),
                        record.get("shipName"),
                        record.get("sailingDate"),
                        record.get("fareCode")
                );
                results.add(normalized);
            }
        }
        return results;
    }
}
