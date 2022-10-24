package com.flores.development.springbatch.config;

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

import com.flores.development.springbatch.model.Customer;

@Configuration
@EnableBatchProcessing
public class FixedWidthReaderConfig {
    
	@Autowired
    public JobBuilderFactory jobBuilder;
    
    @Autowired
    public StepBuilderFactory stepBuilder;
    
    @Bean
    @StepScope
    public FlatFileItemReader<Customer> reader(@Value("#{jobParameters['inputFile']}") String inputFile) {
    	Resource resource = new FileSystemResource("./src/main/resources/customers.csv");
    	
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
    	//just write them to standard out for now
    	return (items) -> items.forEach(System.out::println);
    }
    
    @Bean
    public Step fixedWidthReadingStep() {
    	return stepBuilder.get("fixed-width-reading-step")
    			.<Customer, Customer>chunk(5)
    			.reader(reader(null))
    			.writer(writer())
    			.build();
    }

    @Bean
    public Job sampleBatchJob() {
    	return jobBuilder.get("sample-batch-job")
    			.flow(fixedWidthReadingStep())
    			.end()
    			.build();
    }
}
