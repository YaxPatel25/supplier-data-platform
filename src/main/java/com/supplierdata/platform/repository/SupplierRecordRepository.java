package com.supplierdata.platform.repository;

import com.supplierdata.platform.domain.entity.SupplierRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRecordRepository extends JpaRepository<SupplierRecord, Long> {
    List<SupplierRecord> findByStatus(String status);
    List<SupplierRecord> findBySupplierCode(String supplierCode);
}
