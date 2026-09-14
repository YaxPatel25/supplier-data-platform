package com.supplierdata.platform.service;

import com.supplierdata.platform.domain.entity.JobExecutionLog;
import com.supplierdata.platform.domain.entity.SupplierRecord;
import com.supplierdata.platform.repository.JobExecutionLogRepository;
import com.supplierdata.platform.repository.SupplierRecordRepository;
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
 * Quartz scheduled job for outbound file generation and secure FTP transfer.
 * Periodically pulls normalized records from database, generates outbound files (CSV/JSON/XML/TEXT),
 * transfers them via FTP/SFTP, and records audit metrics in JobExecutionLog persistence store.
 */
@Component
public class ScheduledSupplierExportJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(ScheduledSupplierExportJob.class);

    @Autowired
    private SupplierRecordRepository supplierRecordRepository;

    @Autowired
    private OutboundFileGeneratorService outboundFileGeneratorService;

    @Autowired
    private FtpStorageService ftpStorageService;

    @Autowired
    private JobExecutionLogRepository jobExecutionLogRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getKey().getName();
        String jobGroup = context.getJobDetail().getKey().getGroup();
        log.info("Executing ScheduledSupplierExportJob: {}/{}", jobGroup, jobName);

        JobExecutionLog auditLog = new JobExecutionLog(jobName, jobGroup, "SCHEDULED", "RUNNING");
        auditLog = jobExecutionLogRepository.save(auditLog);

        try {
            JobDataMap dataMap = context.getMergedJobDataMap();
            String exportFormat = dataMap.getString("exportFormat") != null ? dataMap.getString("exportFormat") : "CSV";
            String remoteFileName = dataMap.getString("remoteFileName") != null ? dataMap.getString("remoteFileName") : "outbound-export-" + System.currentTimeMillis() + "." + exportFormat.toLowerCase();

            List<SupplierRecord> records = supplierRecordRepository.findAll();

            String outboundContent = outboundFileGeneratorService.generateOutboundFile(records, exportFormat);

            String targetUri = ftpStorageService.uploadFile(remoteFileName, outboundContent);

            auditLog.setStatus("SUCCESS");
            auditLog.setRecordsProcessed(records.size());
            auditLog.setTargetLocation(targetUri);
            auditLog.setDetails("Exported " + records.size() + " records in " + exportFormat + " format to " + targetUri);
            auditLog.setEndTime(Instant.now());
            jobExecutionLogRepository.save(auditLog);

            log.info("ScheduledSupplierExportJob finished successfully. Exported {} records to {}", records.size(), targetUri);

        } catch (Exception e) {
            log.error("ScheduledSupplierExportJob failed: {}", e.getMessage(), e);
            auditLog.setStatus("FAILED");
            auditLog.setDetails("Export failed: " + e.getMessage());
            auditLog.setEndTime(Instant.now());
            jobExecutionLogRepository.save(auditLog);
            throw new JobExecutionException(e);
        }
    }
}
