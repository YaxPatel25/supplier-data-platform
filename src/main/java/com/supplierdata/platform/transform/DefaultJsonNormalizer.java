package com.supplierdata.platform.transform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.springframework.stereotype.Component;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultJsonNormalizer implements SupplierNormalizer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getSupplierCode() {
        return "DEFAULT";
    }

    @Override
    public String getSupportedFormat() {
        return "JSON";
    }

    @Override
    public List<NormalizedSupplierRecord> normalize(Reader jsonReader) throws Exception {
        JsonNode root = objectMapper.readTree(jsonReader);
        List<NormalizedSupplierRecord> results = new ArrayList<>();

        JsonNode bookings = root.has("bookings") ? root.get("bookings") : root;
        if (bookings.isArray()) {
            for (JsonNode node : bookings) {
                results.add(mapNode(node));
            }
        } else {
            results.add(mapNode(bookings));
        }
        return results;
    }

    private NormalizedSupplierRecord mapNode(JsonNode node) {
        String supplier = textOrNull(node, "supplier");
        if (supplier == null) {
            supplier = textOrNull(node, "supplierCode");
        }
        return new NormalizedSupplierRecord(
                supplier != null ? supplier : "SUP-JSON-DEFAULT",
                "JSON",
                textOrNull(node, "line"),
                textOrNull(node, "vessel"),
                textOrNull(node, "departure"),
                textOrNull(node, "fare")
        );
    }

    private String textOrNull(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asText() : null;
    }
}
