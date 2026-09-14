package com.supplierdata.platform.config;

import com.supplierdata.platform.service.ScheduledSupplierExportJob;
import com.supplierdata.platform.service.ScheduledSupplierImportJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot Quartz Configuration.
 * Configures scheduled Quartz JobDetails, Triggers, and persistent job execution workflows
 * for inbound supplier ingestion and outbound file exports.
 */
@Configuration
public class QuartzSchedulerConfig {

    @Bean
    public JobDetail supplierImportJobDetail() {
        return JobBuilder.newJob(ScheduledSupplierImportJob.class)
                .withIdentity("supplierImportJob", "INGESTION")
                .usingJobData("sourceType", "DROPZONE")
                .usingJobData("supplierCode", "SUP-CSV-A")
                .usingJobData("format", "CSV")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger supplierImportJobTrigger(JobDetail supplierImportJobDetail) {
        // Runs every hour by default (cron expression: 0 0 * * * ?)
        return TriggerBuilder.newTrigger()
                .forJob(supplierImportJobDetail)
                .withIdentity("supplierImportTrigger", "INGESTION")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?"))
                .build();
    }

    @Bean
    public JobDetail supplierExportJobDetail() {
        return JobBuilder.newJob(ScheduledSupplierExportJob.class)
                .withIdentity("supplierExportJob", "EXPORT")
                .usingJobData("exportFormat", "CSV")
                .usingJobData("remoteFileName", "outbound-supplier-export.csv")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger supplierExportJobTrigger(JobDetail supplierExportJobDetail) {
        // Runs every 6 hours by default (cron expression: 0 0 */6 * * ?)
        return TriggerBuilder.newTrigger()
                .forJob(supplierExportJobDetail)
                .withIdentity("supplierExportTrigger", "EXPORT")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 */6 * * ?"))
                .build();
    }
}
