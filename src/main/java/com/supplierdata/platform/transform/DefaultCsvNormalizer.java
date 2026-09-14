package com.supplierdata.platform.transform;

import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultCsvNormalizer implements SupplierNormalizer {

    @Override
    public String getSupplierCode() {
        return "DEFAULT";
    }

    @Override
    public String getSupportedFormat() {
        return "CSV";
    }

    @Override
    public List<NormalizedSupplierRecord> normalize(Reader csvReader) throws Exception {
        List<NormalizedSupplierRecord> results = new ArrayList<>();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build();

        try (CSVParser parser = new CSVParser(csvReader, format)) {
            for (CSVRecord record : parser) {
                String supplier = record.isMapped("supplierCode") ? record.get("supplierCode") : "SUP-CSV-DEFAULT";
                NormalizedSupplierRecord normalized = new NormalizedSupplierRecord(
                        supplier,
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
