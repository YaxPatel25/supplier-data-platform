package com.supplierdata.platform.repository;

import com.supplierdata.platform.domain.entity.JobExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobExecutionLogRepository extends JpaRepository<JobExecutionLog, Long> {
    List<JobExecutionLog> findByJobNameOrderByStartTimeDesc(String jobName);
    List<JobExecutionLog> findByStatus(String status);
}
