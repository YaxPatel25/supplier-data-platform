package com.supplierdata.platform.dto;

/**
 * The company-standardized internal format that every inbound supplier
 * payload (Text, CSV, XML, JSON) is normalized into, regardless of source
 * format. Equivalent to the internal schema produced by the original
 * FlatFiles + XSLT pipeline.
 */
public class NormalizedSupplierRecord {

    private String supplierCode;
    private String sourceFormat;
    private String cruiseLine;
    private String shipName;
    private String sailingDate;
    private String fareCode;

    public NormalizedSupplierRecord() {}

    public NormalizedSupplierRecord(String supplierCode, String sourceFormat, String cruiseLine,
                                     String shipName, String sailingDate, String fareCode) {
        this.supplierCode = supplierCode;
        this.sourceFormat = sourceFormat;
        this.cruiseLine = cruiseLine;
        this.shipName = shipName;
        this.sailingDate = sailingDate;
        this.fareCode = fareCode;
    }

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

    @Override
    public String toString() {
        return "NormalizedSupplierRecord{" +
                "supplierCode='" + supplierCode + '\'' +
                ", sourceFormat='" + sourceFormat + '\'' +
                ", cruiseLine='" + cruiseLine + '\'' +
                ", shipName='" + shipName + '\'' +
                ", sailingDate='" + sailingDate + '\'' +
                ", fareCode='" + fareCode + '\'' +
                '}';
    }
}
