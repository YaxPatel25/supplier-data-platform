package com.supplierdata.platform.service;

import com.supplierdata.platform.domain.entity.JobExecutionLog;
import com.supplierdata.platform.repository.JobExecutionLogRepository;
import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class QuartzSchedulerJobTest {

    @Autowired
    private ScheduledSupplierImportJob scheduledSupplierImportJob;

    @Autowired
    private ScheduledSupplierExportJob scheduledSupplierExportJob;

    @Autowired
    private JobExecutionLogRepository jobExecutionLogRepository;

    @Test
    void testScheduledSupplierImportJobExecution() throws Exception {
        JobExecutionContext context = Mockito.mock(JobExecutionContext.class);
        JobDetail jobDetail = Mockito.mock(JobDetail.class);
        Mockito.when(context.getJobDetail()).thenReturn(jobDetail);
        Mockito.when(jobDetail.getKey()).thenReturn(new JobKey("testImportJob", "TEST"));

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("sourceType", "DROPZONE");
        jobDataMap.put("supplierCode", "SUP-CSV-A");
        jobDataMap.put("format", "CSV");
        Mockito.when(context.getMergedJobDataMap()).thenReturn(jobDataMap);

        scheduledSupplierImportJob.execute(context);

        List<JobExecutionLog> logs = jobExecutionLogRepository.findByJobNameOrderByStartTimeDesc("testImportJob");
        assertFalse(logs.isEmpty());
        assertEquals("SUCCESS", logs.get(0).getStatus());
        assertTrue(logs.get(0).getRecordsProcessed() > 0);
    }

    @Test
    void testScheduledSupplierExportJobExecution() throws Exception {
        JobExecutionContext context = Mockito.mock(JobExecutionContext.class);
        JobDetail jobDetail = Mockito.mock(JobDetail.class);
        Mockito.when(context.getJobDetail()).thenReturn(jobDetail);
        Mockito.when(jobDetail.getKey()).thenReturn(new JobKey("testExportJob", "TEST"));

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("exportFormat", "CSV");
        jobDataMap.put("remoteFileName", "test-export.csv");
        Mockito.when(context.getMergedJobDataMap()).thenReturn(jobDataMap);

        scheduledSupplierExportJob.execute(context);

        List<JobExecutionLog> logs = jobExecutionLogRepository.findByJobNameOrderByStartTimeDesc("testExportJob");
        assertFalse(logs.isEmpty());
        assertEquals("SUCCESS", logs.get(0).getStatus());
    }
}
