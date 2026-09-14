package com.supplierdata.platform.transform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.springframework.stereotype.Component;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Normalizer strategy for Supplier A JSON feeds.
 * Expects nested structure:
 * { "bookings": [ { "supplier": "SUP-JSON-A", "line": "Royal Caribbean", "vessel": "Symphony of the Seas", "departure": "2026-11-15", "fare": "FLASH25" } ] }
 */
@Component
public class SupplierAJsonNormalizer implements SupplierNormalizer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getSupplierCode() {
        return "SUP-JSON-A";
    }

    @Override
    public String getSupportedFormat() {
        return "JSON";
    }

    @Override
    public List<NormalizedSupplierRecord> normalize(Reader reader) throws Exception {
        JsonNode root = objectMapper.readTree(reader);
        List<NormalizedSupplierRecord> results = new ArrayList<>();

        JsonNode bookings = root.has("bookings") ? root.get("bookings") : root;
        if (bookings.isArray()) {
            for (JsonNode node : bookings) {
                results.add(new NormalizedSupplierRecord(
                        textOrFallback(node, "supplier", "SUP-JSON-A"),
                        "JSON",
                        textOrFallback(node, "line", "Royal Caribbean"),
                        textOrFallback(node, "vessel", "Unknown Vessel"),
                        textOrFallback(node, "departure", "2026-01-01"),
                        textOrFallback(node, "fare", "STANDARD")
                ));
            }
        }
        return results;
    }

    private String textOrFallback(JsonNode node, String field, String fallback) {
        return (node.has(field) && !node.get(field).isNull()) ? node.get(field).asText() : fallback;
    }
}
