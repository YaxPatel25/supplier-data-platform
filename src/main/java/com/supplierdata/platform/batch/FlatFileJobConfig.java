package com.supplierdata.platform.batch;

import com.supplierdata.platform.domain.entity.SupplierRecord;
import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;

/**
 * Spring Batch job that watches a "dropzone" directory for supplier CSV
 * FlatFiles and ingests them in chunks -- the batch-processing equivalent
 * of the original .NET FlatFiles onboarding pipeline. Onboarding a new
 * cruise line supplier means adding a new file pattern / mapping here,
 * mirroring "Developed FlatFiles for onboarding new cruise line suppliers"
 * from the original role.
 *
 * For synchronous, per-request normalization (suppliers POSTing directly
 * to the REST API), see IngestionService, which calls the same transform
 * classes without going through a batch job.
 */
@Configuration
public class FlatFileJobConfig {

    @Value("${supplier.dropzone.path:./dropzone}")
    private String dropzonePath;

    @Bean
    public MultiResourceItemReader<NormalizedSupplierRecord> multiCsvReader() throws IOException {
        MultiResourceItemReader<NormalizedSupplierRecord> reader = new MultiResourceItemReader<>();
        reader.setResources(new PathMatchingResourcePatternResolver()
                .getResources("file:" + dropzonePath + "/*.csv"));
        reader.setDelegate(csvFlatFileItemReader());
        return reader;
    }

    private FlatFileItemReader<NormalizedSupplierRecord> csvFlatFileItemReader() {
        FlatFileItemReader<NormalizedSupplierRecord> reader = new FlatFileItemReader<>();
        reader.setLinesToSkip(1); // header row
        DefaultLineMapper<NormalizedSupplierRecord> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("supplierCode", "cruiseLine", "shipName", "sailingDate", "fareCode");
        lineMapper.setLineTokenizer(tokenizer);

        lineMapper.setFieldSetMapper(fieldSet -> new NormalizedSupplierRecord(
                fieldSet.readString("supplierCode"),
                "CSV",
                fieldSet.readString("cruiseLine"),
                fieldSet.readString("shipName"),
                fieldSet.readString("sailingDate"),
                fieldSet.readString("fareCode")
        ));
        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    public Job supplierIngestionJob(JobRepository jobRepository, Step normalizeAndPersistStep) {
        return new JobBuilder("supplierIngestionJob", jobRepository)
                .start(normalizeAndPersistStep)
                .build();
    }

    @Bean
    public Step normalizeAndPersistStep(JobRepository jobRepository,
                                         PlatformTransactionManager transactionManager,
                                         MultiResourceItemReader<NormalizedSupplierRecord> multiCsvReader,
                                         ItemProcessor<NormalizedSupplierRecord, SupplierRecord> supplierRecordProcessor,
                                         ItemWriter<SupplierRecord> supplierRecordWriter) {
        return new StepBuilder("normalizeAndPersistStep", jobRepository)
                .<NormalizedSupplierRecord, SupplierRecord>chunk(50, transactionManager)
                .reader(multiCsvReader)
                .processor(supplierRecordProcessor)
                .writer(supplierRecordWriter)
                .build();
    }
}
