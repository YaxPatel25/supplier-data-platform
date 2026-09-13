package com.supplierdata.platform.repository;

import com.supplierdata.platform.domain.entity.FareCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FareCodeRepository extends JpaRepository<FareCode, Long> {
    Optional<FareCode> findByCode(String code);
}
