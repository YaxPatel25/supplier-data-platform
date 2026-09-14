package com.supplierdata.platform.transform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.springframework.stereotype.Component;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Normalizer strategy for Supplier B JSON feeds.
 * Expects array structure with distinct key naming:
 * [ { "supplierCode": "SUP-JSON-B", "cruiseLine": "Carnival Cruise", "shipName": "Mardi Gras", "sailingDate": "2026-12-01", "rateCode": "FUN4ALL" } ]
 */
@Component
public class SupplierBJsonNormalizer implements SupplierNormalizer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getSupplierCode() {
        return "SUP-JSON-B";
    }

    @Override
    public String getSupportedFormat() {
        return "JSON";
    }

    @Override
    public List<NormalizedSupplierRecord> normalize(Reader reader) throws Exception {
        JsonNode root = objectMapper.readTree(reader);
        List<NormalizedSupplierRecord> results = new ArrayList<>();

        if (root.isArray()) {
            for (JsonNode node : root) {
                results.add(mapItem(node));
            }
        } else if (root.has("items") && root.get("items").isArray()) {
            for (JsonNode node : root.get("items")) {
                results.add(mapItem(node));
            }
        } else {
            results.add(mapItem(root));
        }
        return results;
    }

    private NormalizedSupplierRecord mapItem(JsonNode node) {
        String code = textOrFallback(node, "supplierCode", "SUP-JSON-B");
        String line = textOrFallback(node, "cruiseLine", textOrFallback(node, "operator", "Carnival Cruise"));
        String ship = textOrFallback(node, "shipName", textOrFallback(node, "ship", "Mardi Gras"));
        String sailing = textOrFallback(node, "sailingDate", textOrFallback(node, "sailing", "2026-12-01"));
        String fare = textOrFallback(node, "rateCode", textOrFallback(node, "promoCode", "FUN4ALL"));

        return new NormalizedSupplierRecord(code, "JSON", line, ship, sailing, fare);
    }

    private String textOrFallback(JsonNode node, String field, String fallback) {
        return (node.has(field) && !node.get(field).isNull()) ? node.get(field).asText() : fallback;
    }
}
