package com.supplierdata.platform.batch;

import com.supplierdata.platform.domain.entity.SupplierRecord;
import com.supplierdata.platform.repository.SupplierRecordRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class SupplierRecordWriter implements ItemWriter<SupplierRecord> {

    private final SupplierRecordRepository repository;

    public SupplierRecordWriter(SupplierRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public void write(Chunk<? extends SupplierRecord> chunk) {
        repository.saveAll(chunk.getItems());
    }
}
