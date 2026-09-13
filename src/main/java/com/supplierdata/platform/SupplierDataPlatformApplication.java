package com.supplierdata.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for the Supplier Data Integration Platform.
 *
 * Spring Boot port of a .NET/ASP.NET Core cruise-industry supplier data
 * pipeline: multi-format ingestion (Text/CSV/XML/JSON), XSLT/JSON schema
 * normalization, a Drools-backed Fare Code rules engine, office/site
 * configuration, and REST APIs for both inbound supplier submissions and
 * outbound live supplier API consumption (CruiseCache-style).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class SupplierDataPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(SupplierDataPlatformApplication.class, args);
    }
}
