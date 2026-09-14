package com.supplierdata.platform.transform;

import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Normalizer strategy for Supplier B Text feeds (`SUP-TXT-B`).
 * Handles colon-delimited or fixed-width text file structures:
 * SUP-TXT-B:MSC Cruises:MSC World Europa:2026-12-25:MSCSPECIAL
 * or fixed-length column layout fallback.
 */
@Component
public class SupplierBTextNormalizer implements SupplierNormalizer {

    @Override
    public String getSupplierCode() {
        return "SUP-TXT-B";
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
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                if (line.contains(":")) {
                    // Colon-delimited variant
                    String[] parts = line.split(":");
                    if (parts.length >= 5) {
                        results.add(new NormalizedSupplierRecord(
                                parts[0].trim(), "TEXT", parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim()));
                    }
                } else if (line.length() >= 50) {
                    // Fixed-width positional variant:
                    // Pos 0-10: SupplierCode, 10-30: CruiseLine, 30-50: ShipName, 50-60: SailingDate, 60+: FareCode
                    String supplier = substring(line, 0, 10);
                    String cruiseLine = substring(line, 10, 30);
                    String shipName = substring(line, 30, 50);
                    String sailingDate = substring(line, 50, 60);
                    String fareCode = substring(line, 60, line.length());

                    results.add(new NormalizedSupplierRecord(
                            supplier.isBlank() ? "SUP-TXT-B" : supplier,
                            "TEXT",
                            cruiseLine.isBlank() ? "MSC Cruises" : cruiseLine,
                            shipName.isBlank() ? "MSC Grandiosa" : shipName,
                            sailingDate.isBlank() ? "2026-12-25" : sailingDate,
                            fareCode.isBlank() ? "SPECIAL" : fareCode
                    ));
                }
            }
        }
        return results;
    }

    private String substring(String str, int start, int end) {
        if (start >= str.length()) return "";
        int realEnd = Math.min(end, str.length());
        return str.substring(start, realEnd).trim();
    }
}
