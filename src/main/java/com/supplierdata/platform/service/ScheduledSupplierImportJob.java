package com.supplierdata.platform.service;

import com.supplierdata.platform.domain.entity.JobExecutionLog;
import com.supplierdata.platform.domain.entity.SupplierRecord;
import com.supplierdata.platform.repository.JobExecutionLogRepository;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Quartz scheduled job for automated supplier data import.
 * Periodically polls external sources (FTP, Azure Data Lake, or Dropzone),
 * ingests & normalizes payloads, and logs job execution metrics to persistence store.
 */
@Component
public class ScheduledSupplierImportJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(ScheduledSupplierImportJob.class);

    @Autowired
    private IngestionService ingestionService;

    @Autowired
    private JobExecutionLogRepository jobExecutionLogRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getKey().getName();
        String jobGroup = context.getJobDetail().getKey().getGroup();
        log.info("Executing ScheduledSupplierImportJob: {}/{}", jobGroup, jobName);

        JobExecutionLog auditLog = new JobExecutionLog(jobName, jobGroup, "SCHEDULED", "RUNNING");
        auditLog = jobExecutionLogRepository.save(auditLog);

        try {
            JobDataMap dataMap = context.getMergedJobDataMap();
            String sourceType = dataMap.getString("sourceType") != null ? dataMap.getString("sourceType") : "DROPZONE";
            String supplierCode = dataMap.getString("supplierCode") != null ? dataMap.getString("supplierCode") : "SUP-CSV-A";
            String format = dataMap.getString("format") != null ? dataMap.getString("format") : "CSV";

            List<SupplierRecord> records;
            if ("AZURE_DATA_LAKE".equalsIgnoreCase(sourceType)) {
                String container = dataMap.getString("containerName") != null ? dataMap.getString("containerName") : "inbound";
                String filePath = dataMap.getString("filePath") != null ? dataMap.getString("filePath") : "sample-supplier-feed.csv";
                records = ingestionService.ingestFromAzureDataLake(supplierCode, format, container, filePath);
                auditLog.setTargetLocation("adls://" + container + "/" + filePath);
            } else if ("FTP".equalsIgnoreCase(sourceType)) {
                String remotePath = dataMap.getString("remotePath") != null ? dataMap.getString("remotePath") : "sample-supplier-feed.csv";
                records = ingestionService.ingestFromFtp(supplierCode, format, remotePath);
                auditLog.setTargetLocation("ftp://" + remotePath);
            } else {
                // Dropzone inline sample payload import fallback
                String samplePayload = "supplierCode,cruiseLine,shipName,sailingDate,fareCode\n" + supplierCode + ",Royal Seas,Ocean Star,2026-11-02,PROMO10";
                records = ingestionService.ingest(supplierCode, format, "DROPZONE", samplePayload, "scheduled-dropzone-job");
                auditLog.setTargetLocation("dropzone://scheduled-import");
            }

            auditLog.setStatus("SUCCESS");
            auditLog.setRecordsProcessed(records.size());
            auditLog.setDetails("Successfully imported " + records.size() + " records");
            auditLog.setEndTime(Instant.now());
            jobExecutionLogRepository.save(auditLog);

            log.info("ScheduledSupplierImportJob finished successfully with {} records", records.size());

        } catch (Exception e) {
            log.error("ScheduledSupplierImportJob failed: {}", e.getMessage(), e);
            auditLog.setStatus("FAILED");
            auditLog.setDetails("Import failed: " + e.getMessage());
            auditLog.setEndTime(Instant.now());
            jobExecutionLogRepository.save(auditLog);
            throw new JobExecutionException(e);
        }
    }
}
