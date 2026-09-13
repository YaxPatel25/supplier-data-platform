package com.supplierdata.platform.transform;

import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvNormalizerTest {

    private final CsvNormalizer normalizer = new CsvNormalizer();

    @Test
    void normalizesCsvRowsIntoInternalSchema() throws Exception {
        String csv = """
                supplierCode,cruiseLine,shipName,sailingDate,fareCode
                SUP-001,Royal Seas,Ocean Star,2026-11-02,PROMO10
                SUP-001,Royal Seas,Ocean Star,2026-11-09,STD01
                """;

        List<NormalizedSupplierRecord> results = normalizer.normalize(new StringReader(csv));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getSourceFormat()).isEqualTo("CSV");
        assertThat(results.get(0).getFareCode()).isEqualTo("PROMO10");
        assertThat(results.get(1).getSailingDate()).isEqualTo("2026-11-09");
    }
}
