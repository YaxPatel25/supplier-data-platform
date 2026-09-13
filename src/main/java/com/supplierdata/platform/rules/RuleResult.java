package com.supplierdata.platform.rules;

import java.util.ArrayList;
import java.util.List;

/**
 * Accumulates outcomes produced by the Drools rules as they fire against a
 * FareCodeFact -- e.g. approval status, applied surcharges, flags for
 * manual review.
 */
public class RuleResult {

    private boolean approved = true;
    private final List<String> notes = new ArrayList<>();

    public void flagForReview(String reason) {
        this.approved = false;
        notes.add(reason);
    }

    public void addNote(String note) {
        notes.add(note);
    }

    public boolean isApproved() { return approved; }
    public List<String> getNotes() { return notes; }
}
