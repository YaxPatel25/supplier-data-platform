package com.supplierdata.platform.repository;

import com.supplierdata.platform.domain.entity.Office;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfficeRepository extends JpaRepository<Office, Long> {
    Optional<Office> findByOfficeCode(String officeCode);
}
