package com.flores.dev.springbatch.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.flores.dev.springbatch.model.Employee;
import com.flores.dev.springbatch.parsing.EmployeeBuilderFieldSetMapper;
import com.flores.dev.springbatch.parsing.WorkItemFieldSetMapper;
import com.flores.dev.springbatch.reader.EmployeeFileReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("multi")
@Configuration
@EnableBatchProcessing
public class MultiRecordParsingConfig {

	@Autowired
	public JobBuilderFactory jobBuilder;
	
	@Autowired
	public StepBuilderFactory stepBuilder;
	
	@Bean
	@StepScope
	public FlatFileItemReader<Object> employeeItemReader(@Value("#{jobParameters['inputFile']}") String inputFile) {
		log.info("Initializing multi-line employee item reader");

		Resource resource = new FileSystemResource(inputFile);
		
		return new FlatFileItemReaderBuilder<Object>()
				.name("employeeReader")
				.lineMapper(compositeLineMapper())
				.resource(resource)
				.build();
	}

	public EmployeeFileReader employeeFileReader() {
		return new EmployeeFileReader(employeeItemReader(null));
	}
	
	@Bean
	public PatternMatchingCompositeLineMapper<Object> compositeLineMapper() {
		log.info("Initializing composite line mapper");
		
		final String EMP_KEY = "EMP*";
		final String WRK_KEY = "WRK*";
		
		//create tokenizers for each type of line in our file
		Map<String, LineTokenizer> lineTokenizers = new HashMap<>();
		lineTokenizers.put(EMP_KEY, employeeTokenizer());
		lineTokenizers.put(WRK_KEY, workItemTokenizer());
		
		//create fieldset mappers for each type of line in our file
		Map<String, FieldSetMapper<Object>> fieldsetMappers = new HashMap<>();
		fieldsetMappers.put(EMP_KEY, new EmployeeBuilderFieldSetMapper());
		fieldsetMappers.put(WRK_KEY, new WorkItemFieldSetMapper());
		
		PatternMatchingCompositeLineMapper<Object> lineMappers = new PatternMatchingCompositeLineMapper<>();
		lineMappers.setTokenizers(lineTokenizers);
		lineMappers.setFieldSetMappers(fieldsetMappers);
		return lineMappers;
	}
	
	@Bean
	public LineTokenizer employeeTokenizer() {
		DelimitedLineTokenizer employeeTokenizer = new DelimitedLineTokenizer();
		employeeTokenizer.setNames(new String[] {
				"prefix", "id", "dept_id", "title", "name", "birth_date"
		});
		
		return employeeTokenizer;
	}

	@Bean
	public LineTokenizer workItemTokenizer() {
		DelimitedLineTokenizer workItemTokenizer = new DelimitedLineTokenizer();
		workItemTokenizer.setNames(new String[] {
				"prefix", "id", "title"
		});

		return workItemTokenizer;
	}
	
	@Bean
	@StepScope
	public ItemWriter<Employee> employeeItemWriter() {
		log.info("Initializing employee item writer");
		
		return (employees) -> {
			employees.forEach(e -> log.info(e.toString()));
		};
	}

	@Bean
	public Step multiRecordParsingStep() {
		return stepBuilder.get("multiRecordParsingStep")
				.<Employee, Employee>chunk(2)
				.reader(employeeFileReader())
				.writer(employeeItemWriter())
				.build();
	}

	@Bean
	public Job multiRecordParsingJob() {
		return jobBuilder.get("MultiRecordParsingJob")
				.flow(multiRecordParsingStep())
				.end()
				.build();
	}
}
