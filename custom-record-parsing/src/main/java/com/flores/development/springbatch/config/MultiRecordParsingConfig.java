package com.flores.development.springbatch.config;

import java.util.HashMap;
import java.util.Map;

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

import com.flores.development.springbatch.model.Employee;
import com.flores.development.springbatch.parsing.EmployeeBuilderFieldSetMapper;
import com.flores.development.springbatch.parsing.WorkItemFieldSetMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("multi")
@Configuration
@EnableBatchProcessing
public class MultiRecordParsingConfig {

	@Autowired
	public JobBuilderFactory jobFactory;
	
	@Autowired
	public StepBuilderFactory stepFactory;
	
	@Bean
	@StepScope
	public FlatFileItemReader<Employee> employeeReader(@Value("#{jobParameters['inputFile']") String inputFile) {
		log.info("Initializing multi-line employee reader");

		return new FlatFileItemReaderBuilder<Employee>()
				.name("employeeReader")
				.lineMapper(null)
				.fieldSetMapper(null)
				.build();
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
				"prefix", "id", "dept_id", "title", "name"
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
	public ItemWriter<Employee> employeeWriter() {
		log.info("Initializing employee writer");
		
		return (employees) -> {
			employees.forEach(e -> log.info(e.toString()));
		};
	}
}
