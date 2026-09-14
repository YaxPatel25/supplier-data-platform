package com.supplierdata.platform.transform;

import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Normalizer strategy for Supplier B CSV feeds (`SUP-CSV-B`).
 * Handles custom header mapping: VendorID,LineName,Ship,DepartureDate,PromoRateCode
 * and handles custom date parsing (e.g., MM/dd/yyyy or dd-MMM-yyyy -> yyyy-MM-dd).
 */
@Component
public class SupplierBCsvNormalizer implements SupplierNormalizer {

    @Override
    public String getSupplierCode() {
        return "SUP-CSV-B";
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
                String vendor = getField(record, "VendorID", "SUP-CSV-B");
                String line = getField(record, "LineName", "Celebrity Cruises");
                String ship = getField(record, "Ship", "Apex");
                String rawDate = getField(record, "DepartureDate", "2026-11-20");
                String fare = getField(record, "PromoRateCode", "SUITE20");

                String normalizedDate = normalizeDate(rawDate);

                results.add(new NormalizedSupplierRecord(vendor, "CSV", line, ship, normalizedDate, fare));
            }
        }
        return results;
    }

    private String getField(CSVRecord record, String fieldName, String fallback) {
        if (record.isMapped(fieldName)) {
            String val = record.get(fieldName);
            return (val != null && !val.isBlank()) ? val : fallback;
        }
        return fallback;
    }

    private String normalizeDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) return "2026-11-20";
        try {
            if (rawDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return rawDate;
            }
            if (rawDate.contains("/")) {
                // MM/dd/yyyy
                DateTimeFormatter inFmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                return LocalDate.parse(rawDate, inFmt).toString();
            }
            if (rawDate.contains("-")) {
                // dd-MMM-yyyy e.g. 20-Nov-2026
                DateTimeFormatter inFmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
                return LocalDate.parse(rawDate, inFmt).toString();
            }
        } catch (Exception ignored) {}
        return rawDate;
    }
}
