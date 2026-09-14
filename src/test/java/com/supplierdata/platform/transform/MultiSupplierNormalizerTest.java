package com.supplierdata.platform.transform;

import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MultiSupplierNormalizerTest {

    @Autowired
    private SupplierNormalizationRegistry registry;

    @Test
    void testSupplierAJsonNormalizer() throws Exception {
        String jsonPayload = """
                {
                  "bookings": [
                    { "supplier": "SUP-JSON-A", "line": "Royal Caribbean", "vessel": "Symphony of the Seas", "departure": "2026-11-15", "fare": "FLASH25" }
                  ]
                }
                """;

        List<NormalizedSupplierRecord> records = registry.normalize("SUP-JSON-A", "JSON", new StringReader(jsonPayload));
        assertEquals(1, records.size());
        assertEquals("SUP-JSON-A", records.get(0).getSupplierCode());
        assertEquals("Royal Caribbean", records.get(0).getCruiseLine());
        assertEquals("Symphony of the Seas", records.get(0).getShipName());
        assertEquals("2026-11-15", records.get(0).getSailingDate());
        assertEquals("FLASH25", records.get(0).getFareCode());
    }

    @Test
    void testSupplierBJsonNormalizer() throws Exception {
        String jsonPayload = """
                [
                  { "supplierCode": "SUP-JSON-B", "cruiseLine": "Carnival Cruise", "shipName": "Mardi Gras", "sailingDate": "2026-12-01", "rateCode": "FUN4ALL" }
                ]
                """;

        List<NormalizedSupplierRecord> records = registry.normalize("SUP-JSON-B", "JSON", new StringReader(jsonPayload));
        assertEquals(1, records.size());
        assertEquals("SUP-JSON-B", records.get(0).getSupplierCode());
        assertEquals("Carnival Cruise", records.get(0).getCruiseLine());
        assertEquals("Mardi Gras", records.get(0).getShipName());
        assertEquals("2026-12-01", records.get(0).getSailingDate());
        assertEquals("FUN4ALL", records.get(0).getFareCode());
    }

    @Test
    void testSupplierBCsvNormalizerCustomHeadersAndDateFormat() throws Exception {
        String csvPayload = "VendorID,LineName,Ship,DepartureDate,PromoRateCode\n" +
                            "SUP-CSV-B,Celebrity Cruises,Apex,11/20/2026,SUITE20";

        List<NormalizedSupplierRecord> records = registry.normalize("SUP-CSV-B", "CSV", new StringReader(csvPayload));
        assertEquals(1, records.size());
        assertEquals("SUP-CSV-B", records.get(0).getSupplierCode());
        assertEquals("Celebrity Cruises", records.get(0).getCruiseLine());
        assertEquals("Apex", records.get(0).getShipName());
        assertEquals("2026-11-20", records.get(0).getSailingDate());
        assertEquals("SUITE20", records.get(0).getFareCode());
    }

    @Test
    void testSupplierBXmlNormalizerAttributeBasedXml() throws Exception {
        String xmlPayload = """
                <VoyageFeed supplier="SUP-XML-B">
                  <Sailing line="Viking River" ship="Viking Sky" date="2026-10-10">
                    <Pricing code="VIK-EARLY"/>
                  </Sailing>
                </VoyageFeed>
                """;

        List<NormalizedSupplierRecord> records = registry.normalize("SUP-XML-B", "XML", new StringReader(xmlPayload));
        assertEquals(1, records.size());
        assertEquals("SUP-XML-B", records.get(0).getSupplierCode());
        assertEquals("Viking River", records.get(0).getCruiseLine());
        assertEquals("Viking Sky", records.get(0).getShipName());
        assertEquals("2026-10-10", records.get(0).getSailingDate());
        assertEquals("VIK-EARLY", records.get(0).getFareCode());
    }

    @Test
    void testSupplierBTextNormalizerColonDelimited() throws Exception {
        String textPayload = "SUP-TXT-B:MSC Cruises:MSC World Europa:2026-12-25:MSCSPECIAL\n";

        List<NormalizedSupplierRecord> records = registry.normalize("SUP-TXT-B", "TEXT", new StringReader(textPayload));
        assertEquals(1, records.size());
        assertEquals("SUP-TXT-B", records.get(0).getSupplierCode());
        assertEquals("MSC Cruises", records.get(0).getCruiseLine());
        assertEquals("MSC World Europa", records.get(0).getShipName());
        assertEquals("2026-12-25", records.get(0).getSailingDate());
        assertEquals("MSCSPECIAL", records.get(0).getFareCode());
    }
}
