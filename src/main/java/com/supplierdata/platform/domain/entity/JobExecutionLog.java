package com.supplierdata.platform.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity to audit and persist Quartz scheduled job executions (Import and Export jobs),
 * tracking status, execution timing, records processed, and failure reasons.
 */
@Entity
@Table(name = "job_execution_logs")
public class JobExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String jobName;

    @Column(nullable = false)
    private String jobGroup;

    @Column(nullable = false)
    private String status; // RUNNING, SUCCESS, FAILED

    @Column(nullable = false)
    private String triggerType; // CRON, MANUAL, SIMPLE

    private int recordsProcessed;

    private String targetLocation;

    @Column(length = 2000)
    private String details;

    @Column(nullable = false)
    private Instant startTime = Instant.now();

    private Instant endTime;

    public JobExecutionLog() {}

    public JobExecutionLog(String jobName, String jobGroup, String triggerType, String status) {
        this.jobName = jobName;
        this.jobGroup = jobGroup;
        this.triggerType = triggerType;
        this.status = status;
        this.startTime = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }

    public String getJobGroup() { return jobGroup; }
    public void setJobGroup(String jobGroup) { this.jobGroup = jobGroup; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }

    public int getRecordsProcessed() { return recordsProcessed; }
    public void setRecordsProcessed(int recordsProcessed) { this.recordsProcessed = recordsProcessed; }

    public String getTargetLocation() { return targetLocation; }
    public void setTargetLocation(String targetLocation) { this.targetLocation = targetLocation; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
}
