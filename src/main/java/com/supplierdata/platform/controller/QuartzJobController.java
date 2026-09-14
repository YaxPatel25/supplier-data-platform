package com.supplierdata.platform.controller;

import com.supplierdata.platform.domain.entity.JobExecutionLog;
import com.supplierdata.platform.repository.JobExecutionLogRepository;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for Quartz Scheduler management.
 * Provides APIs to inspect scheduled jobs, trigger import/export jobs on-demand,
 * and review persistent job execution history logs.
 */
@RestController
@RequestMapping("/api/v1/scheduler")
public class QuartzJobController {

    private static final Logger log = LoggerFactory.getLogger(QuartzJobController.class);

    private final Scheduler scheduler;
    private final JobExecutionLogRepository jobExecutionLogRepository;

    public QuartzJobController(Scheduler scheduler, JobExecutionLogRepository jobExecutionLogRepository) {
        this.scheduler = scheduler;
        this.jobExecutionLogRepository = jobExecutionLogRepository;
    }

    public record ImportJobTriggerRequest(
            String supplierCode,
            String format,
            String sourceType,
            String containerName,
            String filePath,
            String remotePath
    ) {}

    public record ExportJobTriggerRequest(
            String exportFormat,
            String remoteFileName
    ) {}

    @GetMapping("/jobs")
    public ResponseEntity<List<Map<String, Object>>> listJobs() throws SchedulerException {
        List<Map<String, Object>> jobList = new ArrayList<>();
        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                JobDetail detail = scheduler.getJobDetail(jobKey);
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);

                Map<String, Object> map = new HashMap<>();
                map.put("name", jobKey.getName());
                map.put("group", jobKey.getGroup());
                map.put("description", detail.getDescription());
                map.put("jobClass", detail.getJobClass().getName());
                map.put("triggerCount", triggers.size());
                jobList.add(map);
            }
        }
        return ResponseEntity.ok(jobList);
    }

    @PostMapping("/jobs/import/trigger")
    public ResponseEntity<Map<String, String>> triggerImportJob(@RequestBody(required = false) ImportJobTriggerRequest request) throws SchedulerException {
        JobDataMap dataMap = new JobDataMap();
        if (request != null) {
            if (request.supplierCode() != null) dataMap.put("supplierCode", request.supplierCode());
            if (request.format() != null) dataMap.put("format", request.format());
            if (request.sourceType() != null) dataMap.put("sourceType", request.sourceType());
            if (request.containerName() != null) dataMap.put("containerName", request.containerName());
            if (request.filePath() != null) dataMap.put("filePath", request.filePath());
            if (request.remotePath() != null) dataMap.put("remotePath", request.remotePath());
        }

        JobKey jobKey = JobKey.jobKey("supplierImportJob", "INGESTION");
        scheduler.triggerJob(jobKey, dataMap);
        log.info("Triggered supplierImportJob manually via REST API");

        return ResponseEntity.ok(Map.of("message", "Triggered supplierImportJob successfully", "job", jobKey.toString()));
    }

    @PostMapping("/jobs/export/trigger")
    public ResponseEntity<Map<String, String>> triggerExportJob(@RequestBody(required = false) ExportJobTriggerRequest request) throws SchedulerException {
        JobDataMap dataMap = new JobDataMap();
        if (request != null) {
            if (request.exportFormat() != null) dataMap.put("exportFormat", request.exportFormat());
            if (request.remoteFileName() != null) dataMap.put("remoteFileName", request.remoteFileName());
        }

        JobKey jobKey = JobKey.jobKey("supplierExportJob", "EXPORT");
        scheduler.triggerJob(jobKey, dataMap);
        log.info("Triggered supplierExportJob manually via REST API");

        return ResponseEntity.ok(Map.of("message", "Triggered supplierExportJob successfully", "job", jobKey.toString()));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<JobExecutionLog>> getLogs() {
        return ResponseEntity.ok(jobExecutionLogRepository.findAll());
    }
}
