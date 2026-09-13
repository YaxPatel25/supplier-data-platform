package com.supplierdata.platform.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Mirrors "Fare Code Rule Types" from the original platform's Rule
 * Management module: a fare category with attributes the Drools rules
 * engine evaluates against (e.g. refundability, discount eligibility).
 */
@Entity
@Table(name = "fare_codes")
public class FareCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String category; // e.g. RESTRICTED, FLEXIBLE, PROMO

    @Column(nullable = false)
    private boolean refundable;

    @Column(nullable = false)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isRefundable() { return refundable; }
    public void setRefundable(boolean refundable) { this.refundable = refundable; }

    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }
}
