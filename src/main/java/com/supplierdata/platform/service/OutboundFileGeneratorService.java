package com.supplierdata.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.supplierdata.platform.domain.entity.SupplierRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.List;

/**
 * Service for outbound file generation. Converts normalized supplier records
 * into target output formats (CSV, JSON, XML, TEXT) for downstream distribution.
 */
@Service
public class OutboundFileGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(OutboundFileGeneratorService.class);
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public String generateOutboundFile(List<SupplierRecord> records, String format) throws Exception {
        if (records == null || records.isEmpty()) {
            log.warn("Generating empty outbound file for format {}", format);
        }

        String fmtUpper = (format != null) ? format.trim().toUpperCase() : "CSV";

        return switch (fmtUpper) {
            case "CSV" -> generateCsv(records);
            case "JSON" -> generateJson(records);
            case "XML" -> generateXml(records);
            case "TEXT" -> generateText(records);
            default -> throw new IllegalArgumentException("Unsupported outbound file format: " + format);
        };
    }

    private String generateCsv(List<SupplierRecord> records) throws Exception {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("supplierCode", "cruiseLine", "shipName", "sailingDate", "fareCode", "status")
                .build();

        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            for (SupplierRecord r : records) {
                printer.printRecord(
                        r.getSupplierCode(),
                        r.getCruiseLine(),
                        r.getShipName(),
                        r.getSailingDate(),
                        r.getFareCode(),
                        r.getStatus()
                );
            }
        }
        return sw.toString();
    }

    private String generateJson(List<SupplierRecord> records) throws Exception {
        return objectMapper.writeValueAsString(records);
    }

    private String generateXml(List<SupplierRecord> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<OutboundSupplierExport>\n");
        for (SupplierRecord r : records) {
            sb.append("  <SupplierRecord>\n");
            sb.append("    <SupplierCode>").append(escapeXml(r.getSupplierCode())).append("</SupplierCode>\n");
            sb.append("    <CruiseLine>").append(escapeXml(r.getCruiseLine())).append("</CruiseLine>\n");
            sb.append("    <ShipName>").append(escapeXml(r.getShipName())).append("</ShipName>\n");
            sb.append("    <SailingDate>").append(escapeXml(r.getSailingDate())).append("</SailingDate>\n");
            sb.append("    <FareCode>").append(escapeXml(r.getFareCode())).append("</FareCode>\n");
            sb.append("    <Status>").append(escapeXml(r.getStatus())).append("</Status>\n");
            sb.append("  </SupplierRecord>\n");
        }
        sb.append("</OutboundSupplierExport>");
        return sb.toString();
    }

    private String generateText(List<SupplierRecord> records) {
        StringBuilder sb = new StringBuilder();
        for (SupplierRecord r : records) {
            sb.append(r.getSupplierCode()).append("|")
              .append(r.getCruiseLine()).append("|")
              .append(r.getShipName()).append("|")
              .append(r.getSailingDate()).append("|")
              .append(r.getFareCode()).append("\n");
        }
        return sb.toString();
    }

    private String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }
}
