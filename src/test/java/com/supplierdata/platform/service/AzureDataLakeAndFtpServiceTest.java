package com.supplierdata.platform.service;

import com.supplierdata.platform.domain.entity.SupplierRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AzureDataLakeAndFtpServiceTest {

    @Autowired
    private IngestionService ingestionService;

    @Autowired
    private AzureDataLakeStorageService azureDataLakeStorageService;

    @Autowired
    private FtpStorageService ftpStorageService;

    @Autowired
    private OutboundFileGeneratorService outboundFileGeneratorService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() throws Exception {
        // Setup local test files in dropzones
        Path adlsDir = Paths.get(azureDataLakeStorageService.getBasePath(), "inbound");
        Files.createDirectories(adlsDir);
        Path adlsFile = adlsDir.resolve("supplier-a.json");
        Files.writeString(adlsFile, """
                {
                  "bookings": [
                    { "supplier": "SUP-JSON-A", "line": "Royal Seas", "vessel": "Ocean Star", "departure": "2026-11-02", "fare": "PROMO10" }
                  ]
                }
                """);

        Path ftpDir = Paths.get(ftpStorageService.getFtpBasePath(), "inbound");
        Files.createDirectories(ftpDir);
        Path ftpFile = ftpDir.resolve("supplier-b.csv");
        Files.writeString(ftpFile, "VendorID,LineName,Ship,DepartureDate,PromoRateCode\nSUP-CSV-B,Celebrity,Edge,2026-05-10,SUMMER50");
    }

    @Test
    void testIngestFromAzureDataLake() {
        List<SupplierRecord> records = ingestionService.ingestFromAzureDataLake(
                "SUP-JSON-A", "JSON", "inbound", "supplier-a.json");
        assertNotNull(records);
        assertEquals(1, records.size());
        assertEquals("AZURE_DATA_LAKE", records.get(0).getSourceType());
        assertEquals("SUP-JSON-A", records.get(0).getSupplierCode());
    }

    @Test
    void testIngestFromFtp() {
        List<SupplierRecord> records = ingestionService.ingestFromFtp(
                "SUP-CSV-B", "CSV", "supplier-b.csv");
        assertNotNull(records);
        assertEquals(1, records.size());
        assertEquals("FTP", records.get(0).getSourceType());
        assertEquals("SUP-CSV-B", records.get(0).getSupplierCode());
    }

    @Test
    void testOutboundFileGeneration() throws Exception {
        SupplierRecord record = new SupplierRecord();
        record.setSupplierCode("SUP-001");
        record.setCruiseLine("Royal Seas");
        record.setShipName("Ocean Star");
        record.setSailingDate("2026-11-02");
        record.setFareCode("PROMO10");
        record.setStatus("NORMALIZED");

        String csvOutput = outboundFileGeneratorService.generateOutboundFile(List.of(record), "CSV");
        assertTrue(csvOutput.contains("SUP-001"));
        assertTrue(csvOutput.contains("Royal Seas"));

        String xmlOutput = outboundFileGeneratorService.generateOutboundFile(List.of(record), "XML");
        assertTrue(xmlOutput.contains("<SupplierCode>SUP-001</SupplierCode>"));

        String jsonOutput = outboundFileGeneratorService.generateOutboundFile(List.of(record), "JSON");
        assertTrue(jsonOutput.contains("\"supplierCode\" : \"SUP-001\""));
    }
}
