package com.supplierdata.platform.rules;

import com.supplierdata.platform.domain.entity.FareCode;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

/**
 * Evaluates a FareCode against the Drools rule base (fare-code-rules.drl),
 * the Spring Boot equivalent of the original platform's Rule Management /
 * Fare Code Rule Types feature.
 */
@Service
public class FareCodeRuleService {

    private final KieContainer kieContainer;

    public FareCodeRuleService() {
        this.kieContainer = KieServices.Factory.get().getKieClasspathContainer();
    }

    public RuleResult evaluate(FareCode fareCode) {
        RuleResult result = new RuleResult();
        FareCodeFact fact = new FareCodeFact(
                fareCode.getCode(),
                fareCode.getCategory(),
                fareCode.isRefundable(),
                fareCode.getDiscountPercentage()
        );

        KieSession session = kieContainer.newKieSession();
        try {
            session.insert(fact);
            session.insert(result);
            session.fireAllRules();
        } finally {
            session.dispose();
        }
        return result;
    }
}
