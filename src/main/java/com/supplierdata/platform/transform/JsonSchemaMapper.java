package com.supplierdata.platform.transform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps supplier JSON payloads (arbitrary shape, per-supplier field names)
 * into the internal schema. Equivalent to the JSON side of the original
 * "XSLT transformations to convert XML and JSON payloads" step -- since
 * XSLT doesn't apply natively to JSON, this uses a configurable field-path
 * mapping instead, which is the more idiomatic Java approach.
 *
 * Default mapping assumes:
 *   { "supplier": "...", "line": "...", "vessel": "...",
 *     "departure": "...", "fare": "..." }
 */
@Component
public class JsonSchemaMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<NormalizedSupplierRecord> normalize(Reader jsonReader) throws IOException {
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
        return new NormalizedSupplierRecord(
                textOrNull(node, "supplier"),
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
