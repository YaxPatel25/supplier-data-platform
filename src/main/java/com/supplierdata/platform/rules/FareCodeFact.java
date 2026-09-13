package com.supplierdata.platform.rules;

import java.math.BigDecimal;

/**
 * Fact object inserted into the Drools working memory representing a
 * supplier record's fare code, evaluated against Fare Code Rule Types
 * (e.g. refundability, discount eligibility, promo restrictions).
 */
public class FareCodeFact {

    private final String fareCode;
    private final String category;
    private final boolean refundable;
    private final BigDecimal discountPercentage;

    public FareCodeFact(String fareCode, String category, boolean refundable, BigDecimal discountPercentage) {
        this.fareCode = fareCode;
        this.category = category;
        this.refundable = refundable;
        this.discountPercentage = discountPercentage;
    }

    public String getFareCode() { return fareCode; }
    public String getCategory() { return category; }
    public boolean isRefundable() { return refundable; }
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
}
