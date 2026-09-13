package com.supplierdata.platform.batch;

import com.supplierdata.platform.domain.entity.SupplierRecord;
import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Converts a normalized supplier DTO (from CSV/XML/JSON/Text parsing) into
 * the persisted SupplierRecord entity, marking it ready for downstream
 * rule evaluation. Equivalent to the transformation stage of the original
 * FlatFiles pipeline.
 */
@Component
public class SupplierRecordProcessor implements ItemProcessor<NormalizedSupplierRecord, SupplierRecord> {

    @Override
    public SupplierRecord process(NormalizedSupplierRecord item) {
        SupplierRecord record = new SupplierRecord();
        record.setSupplierCode(item.getSupplierCode());
        record.setSourceFormat(item.getSourceFormat());
        record.setCruiseLine(item.getCruiseLine());
        record.setShipName(item.getShipName());
        record.setSailingDate(item.getSailingDate());
        record.setFareCode(item.getFareCode());
        record.setRawPayloadReference("batch-import");
        record.setStatus("NORMALIZED");
        return record;
    }
}
