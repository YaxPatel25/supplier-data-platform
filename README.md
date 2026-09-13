# Supplier Data Integration Platform

A Spring Boot port of a .NET/ASP.NET Core cruise-industry supplier data
pipeline: multi-format ingestion, business rule evaluation, and REST APIs
for both inbound supplier submissions and outbound live supplier API
consumption.

## Where each original .NET responsibility landed

| Original (.NET) | Here |
|---|---|
| ASP.NET Core Web API | `controller/` — Spring Web REST controllers |
| FlatFiles (C# transformation modules) | `transform/` (`CsvNormalizer`, `TextFlatFileParser`) + `batch/` (Spring Batch dropzone job) |
| XSLT transformations | `transform/XmlXsltTransformer` + `resources/xslt/supplier-xml-to-internal.xsl` |
| JSON payload transformation | `transform/JsonSchemaMapper` |
| RESTful APIs for supplier submissions | `controller/SupplierApiController` |
| CruiseCache live API integration | `client/CruiseCacheClient` (WebClient + Resilience4j retry/circuit-breaker) |
| Site Preferences / Office Setup | `config/OfficeConfigProperties` + `office.sites.*` in `application.yml` |
| Fare Code Rule Types / Rule Management | `rules/` (Drools: `fare-code-rules.drl`, `FareCodeRuleService`) |
| SQL Server + stored procedures | Spring Data JPA (`repository/`) against SQL Server; entities in `domain/entity/` |
| Postman testing | `postman/SupplierDataPlatform.postman_collection.json` |
| Production support / FlatFile failure monitoring | `GlobalExceptionHandler` + structured logging in `IngestionService` |
| Onboarding new cruise line suppliers | Drop a new supplier's CSV into `dropzone/`, or extend the format switch in `IngestionService` |

## Two ingestion paths

1. **Synchronous REST submission** — a supplier POSTs a payload (any of
   CSV/XML/JSON/TEXT) to `/api/v1/suppliers/submissions`. `IngestionService`
   routes it to the matching normalizer and persists it immediately.
2. **Batch dropzone** — CSV files placed in `./dropzone` are picked up by
   the Spring Batch job (`FlatFileJobConfig`) for bulk/offline onboarding,
   the same pattern as receiving a new supplier's full FlatFile export.

Both paths converge on the same internal schema (`NormalizedSupplierRecord`
→ `SupplierRecord`), matching the original "normalize everything into the
company's standardized internal format" design.

## Running locally

The `dev` profile uses an in-memory H2 database (SQL Server compatibility
mode) so you can run it without a real SQL Server instance:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

For a real SQL Server target, use the `prod` profile and set
`DB_HOST`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

> This sandbox has no access to Maven Central, so the project was written
> but not compiled here. Run `mvn clean install` locally to build and
> resolve dependencies before first use.

## Try it out

Import `postman/SupplierDataPlatform.postman_collection.json`, or curl:

```bash
curl -X POST http://localhost:8080/api/v1/suppliers/submissions \
  -H "Content-Type: application/json" \
  -d '{"format":"CSV","payload":"supplierCode,cruiseLine,shipName,sailingDate,fareCode\nSUP-001,Royal Seas,Ocean Star,2026-11-02,PROMO10"}'
```

Sample payloads for XML/JSON/TEXT formats are in `src/main/resources/sample-data/`.

## Tests

```bash
mvn test
```

Covers CSV/Text normalization, Drools rule outcomes (`FareCodeRuleServiceTest`),
and a MockMvc controller test.

## Suggested next steps for the portfolio

- Add integration tests with Testcontainers against a real SQL Server image
- Add a `SecurityConfig` (API key or OAuth2) for the supplier-facing endpoints
- Wire `IngestionService` failures to a real alerting sink (e.g. Slack webhook)
- Add pagination/filtering to `/api/v1/supplier-records`
