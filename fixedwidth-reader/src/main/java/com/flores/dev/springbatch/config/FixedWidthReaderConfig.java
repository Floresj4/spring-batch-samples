package com.flores.dev.springbatch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.flores.dev.springbatch.listeners.LoggingChunkListener;
import com.flores.dev.springbatch.listeners.LoggingJobExecutionListener;
import com.flores.dev.springbatch.listeners.LoggingStepExecutionListener;
import com.flores.dev.springbatch.model.Customer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
public class FixedWidthReaderConfig {
    
	@Autowired
    public JobBuilderFactory jobBuilder;
    
    @Autowired
    public StepBuilderFactory stepBuilder;

    public static final int CHUNK_SIZE = 2;

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> reader(@Value("#{jobParameters['inputFile']}") String inputFile) {
    	log.debug("Initializing reader with input source {}", inputFile);

    	Resource resource = new FileSystemResource(inputFile);
    	
    	//column name and field lengths
    	String[] columnNames = new String[] {"name", "address", "phone"};
    	Range[] ranges = new Range[]{
    			new Range(1, 15),
    			new Range(15, 41),
    			new Range(42, 53)
    	};
    	
    	return new FlatFileItemReaderBuilder<Customer>()
    			.name("custom-reader")
    			.resource(resource)
    			.fixedLength()
    			.columns(ranges)
    			.names(columnNames)
    			.targetType(Customer.class)
    			.build();
    }
    
    @Bean
    @StepScope
    public ItemWriter<Customer> writer() {
    	log.debug("Initializing logging writer");

    	return (items) -> items.forEach(i -> log.debug(i.toString()));
    }
    
    @Bean
    public Step fixedWidthReadingStep() {
    	log.debug("Initializing fixed width reading step.  Chunk size: {}");

    	return stepBuilder.get("fixed-width-reading-step")
    			.<Customer, Customer>chunk(CHUNK_SIZE)
    			.reader(reader(null))	//null placeholder for late-bind jobParam
    			.writer(writer())
    			.listener(new LoggingStepExecutionListener())
    			.listener(new LoggingChunkListener())
    			.build();
    }

    @Bean
    public Job fixedWidthBatchJob() {
    	log.debug("Initializing batch job for fixed width file reading");

    	return jobBuilder.get("sample-batch-job")
    			.listener(new LoggingJobExecutionListener())
    			.flow(fixedWidthReadingStep())
    			.end()
    			.build();
    }
}
