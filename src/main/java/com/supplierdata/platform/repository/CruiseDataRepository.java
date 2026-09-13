package com.supplierdata.platform.repository;

import com.supplierdata.platform.domain.entity.CruiseData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CruiseDataRepository extends JpaRepository<CruiseData, Long> {
    List<CruiseData> findByShipNameAndSailingDate(String shipName, String sailingDate);
}
