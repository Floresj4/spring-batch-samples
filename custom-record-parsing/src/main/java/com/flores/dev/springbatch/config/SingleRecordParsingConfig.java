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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.flores.dev.springbatch.model.Employee;
import com.flores.dev.springbatch.parsing.EmployeeFieldSetMapper;
import com.flores.dev.springbatch.parsing.EmployeeLineTokenizer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("single")
@Configuration
@EnableBatchProcessing
public class SingleRecordParsingConfig {

	@Autowired
	private JobBuilderFactory jobBuilder;
	
	@Autowired
	private StepBuilderFactory stepBuilder;

	//this would be externalized in an ideal scenario
	private static final int CHUNK_SIZE = 10;
	
	@Bean
	@StepScope
	public FlatFileItemReader<Employee> employeeReader(@Value("#{jobParameters['inputFile']}") String inputFile) {
		Resource resource = new FileSystemResource(inputFile);

		//fields used in the spring-managed delimited() reader
//		String fieldNames[] = new String[] {"id", "dept_id", "title", "name"};
		
		return new FlatFileItemReaderBuilder<Employee>()
				.resource(resource)
				.name("employeeReader")
				.linesToSkip(1)		//skip the header row
				.lineTokenizer(new EmployeeLineTokenizer())
				.fieldSetMapper(new EmployeeFieldSetMapper())
//				.delimited()			//spring-managed approach to simple delimited files
//				.names(fieldNames)
//				.targetType(Employee.class)
				.build();
	}
	
	@Bean
	@StepScope
	public ItemWriter<Employee> employeeWriter() {
		return (employees) -> {
			//write the contents of a complete chunk
			employees.forEach(e -> log.info(e.toString()));
		};
	}
	
	@Bean
	public Step customRecordParsingStep() {
		log.debug("Initializing custom record parsing step");
		
		return stepBuilder.get("CustomRecordParsingStep")
				.<Employee, Employee>chunk(CHUNK_SIZE)
				.reader(employeeReader(null))	//null to account for late-bind inputFile
				.writer(employeeWriter())
				.build();
	}
	
	@Bean
	public Job customRecordParsingJob() {
		log.debug("Initializing Custom Record Parsing job...");
		
		return jobBuilder.get("CustomRecordParsingJob")
				.flow(customRecordParsingStep())
				.end()
				.build();
	}
}
