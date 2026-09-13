<?xml version="1.0" encoding="UTF-8"?>
<!--
  Converts a supplier's proprietary XML cruise feed into the platform's
  internal <SupplierRecord> schema. This is the direct equivalent of the
  XSLT step used in the original .NET pipeline before FlatFile processing.

  Expected supplier input shape:
    <SupplierFeed>
      <Booking>
        <Supplier>ABC123</Supplier>
        <Line>Royal Seas</Line>
        <Vessel>Ocean Star</Vessel>
        <Departure>2026-11-02</Departure>
        <Fare>PROMO10</Fare>
      </Booking>
      ...
    </SupplierFeed>
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/SupplierFeed">
        <InternalRecords>
            <xsl:apply-templates select="Booking"/>
        </InternalRecords>
    </xsl:template>

    <xsl:template match="Booking">
        <SupplierRecord>
            <SupplierCode><xsl:value-of select="Supplier"/></SupplierCode>
            <SourceFormat>XML</SourceFormat>
            <CruiseLine><xsl:value-of select="Line"/></CruiseLine>
            <ShipName><xsl:value-of select="Vessel"/></ShipName>
            <SailingDate><xsl:value-of select="Departure"/></SailingDate>
            <FareCode><xsl:value-of select="Fare"/></FareCode>
        </SupplierRecord>
    </xsl:template>
</xsl:stylesheet>
