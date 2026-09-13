package com.supplierdata.platform.rules;

import com.supplierdata.platform.domain.entity.FareCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FareCodeRuleServiceTest {

    private final FareCodeRuleService ruleService = new FareCodeRuleService();

    @Test
    void flagsNonRefundablePromoForManualReview() {
        FareCode fareCode = new FareCode();
        fareCode.setCode("PROMO10");
        fareCode.setCategory("PROMO");
        fareCode.setRefundable(false);
        fareCode.setDiscountPercentage(BigDecimal.TEN);

        RuleResult result = ruleService.evaluate(fareCode);

        assertThat(result.isApproved()).isFalse();
        assertThat(result.getNotes()).anyMatch(note -> note.contains("PROMO10"));
    }

    @Test
    void autoApprovesFlexibleRefundableFare() {
        FareCode fareCode = new FareCode();
        fareCode.setCode("FLEX20");
        fareCode.setCategory("FLEXIBLE");
        fareCode.setRefundable(true);
        fareCode.setDiscountPercentage(BigDecimal.valueOf(20));

        RuleResult result = ruleService.evaluate(fareCode);

        assertThat(result.isApproved()).isTrue();
    }
}
