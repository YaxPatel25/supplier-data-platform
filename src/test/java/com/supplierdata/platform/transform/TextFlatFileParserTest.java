package com.supplierdata.platform.transform;

import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextFlatFileParserTest {

    private final TextFlatFileParser parser = new TextFlatFileParser();

    @Test
    void parsesPipeDelimitedFlatFile() throws Exception {
        String text = "SUP-004|Northern Star Cruises|Aurora Explorer|2026-09-30|PROMO10";

        List<NormalizedSupplierRecord> results = parser.normalize(new StringReader(text));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCruiseLine()).isEqualTo("Northern Star Cruises");
    }

    @Test
    void rejectsMalformedLines() {
        String malformed = "SUP-004|Northern Star Cruises|Aurora Explorer"; // missing fields

        assertThrows(IllegalArgumentException.class,
                () -> parser.normalize(new StringReader(malformed)));
    }
}
