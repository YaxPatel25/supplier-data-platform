package com.supplierdata.platform.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Persisted normalized record produced by the ingestion pipeline, equivalent
 * to what the original FlatFiles modules wrote into the internal database
 * after normalizing Text/CSV/XML/JSON supplier data.
 */
@Entity
@Table(name = "supplier_records")
public class SupplierRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String supplierCode;

    @Column(nullable = false)
    private String sourceFormat; // TEXT, CSV, XML, JSON

    @Column(nullable = false)
    private String cruiseLine;

    @Column(nullable = false)
    private String shipName;

    @Column(nullable = false)
    private String sailingDate;

    @Column(nullable = false)
    private String fareCode;

    @Column(nullable = false)
    private String rawPayloadReference;

    @Column(nullable = false)
    private String status = "RECEIVED"; // RECEIVED, NORMALIZED, RULE_APPLIED, FAILED

    @Column(nullable = false)
    private Instant receivedAt = Instant.now();

    private String failureReason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public String getSourceFormat() { return sourceFormat; }
    public void setSourceFormat(String sourceFormat) { this.sourceFormat = sourceFormat; }

    public String getCruiseLine() { return cruiseLine; }
    public void setCruiseLine(String cruiseLine) { this.cruiseLine = cruiseLine; }

    public String getShipName() { return shipName; }
    public void setShipName(String shipName) { this.shipName = shipName; }

    public String getSailingDate() { return sailingDate; }
    public void setSailingDate(String sailingDate) { this.sailingDate = sailingDate; }

    public String getFareCode() { return fareCode; }
    public void setFareCode(String fareCode) { this.fareCode = fareCode; }

    public String getRawPayloadReference() { return rawPayloadReference; }
    public void setRawPayloadReference(String rawPayloadReference) { this.rawPayloadReference = rawPayloadReference; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
