package com.supplierdata.platform.controller;

import com.supplierdata.platform.domain.entity.FareCode;
import com.supplierdata.platform.repository.FareCodeRepository;
import com.supplierdata.platform.rules.FareCodeRuleService;
import com.supplierdata.platform.rules.RuleResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes Fare Code Rule Type evaluation -- the REST equivalent of the
 * original platform's Rule Management screen, now backed by Drools.
 */
@RestController
@RequestMapping("/api/v1/fare-codes")
public class FareCodeController {

    private final FareCodeRepository fareCodeRepository;
    private final FareCodeRuleService ruleService;

    public FareCodeController(FareCodeRepository fareCodeRepository, FareCodeRuleService ruleService) {
        this.fareCodeRepository = fareCodeRepository;
        this.ruleService = ruleService;
    }

    @PostMapping
    public FareCode create(@RequestBody FareCode fareCode) {
        return fareCodeRepository.save(fareCode);
    }

    @GetMapping("/{code}/evaluate")
    public ResponseEntity<RuleResult> evaluate(@PathVariable String code) {
        return fareCodeRepository.findByCode(code)
                .map(ruleService::evaluate)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
