package com.supplierdata.platform.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Real-time cruise data retrieved from a live supplier API
 * (CruiseCache-style integration), cached locally.
 */
@Entity
@Table(name = "cruise_data")
public class CruiseData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String supplierCode;

    @Column(nullable = false)
    private String shipName;

    @Column(nullable = false)
    private String sailingDate;

    @Column(nullable = false)
    private Integer availableCabins;

    @Column(nullable = false)
    private Instant fetchedAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public String getShipName() { return shipName; }
    public void setShipName(String shipName) { this.shipName = shipName; }

    public String getSailingDate() { return sailingDate; }
    public void setSailingDate(String sailingDate) { this.sailingDate = sailingDate; }

    public Integer getAvailableCabins() { return availableCabins; }
    public void setAvailableCabins(Integer availableCabins) { this.availableCabins = availableCabins; }

    public Instant getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(Instant fetchedAt) { this.fetchedAt = fetchedAt; }
}
