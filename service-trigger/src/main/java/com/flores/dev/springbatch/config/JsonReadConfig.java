package com.flores.dev.springbatch.config;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flores.dev.springbatch.model.Employee;

@Configuration
@EnableBatchProcessing
public class JsonReadConfig {

	@Autowired
	private JobBuilderFactory jobFactory;
	
	@Autowired
	private StepBuilderFactory stepFactory;

	@Bean
	@StepScope
	public ItemStreamReader<Employee> reader(@Value("#{jobParameters['inputFile']}") String inputFile) {		

		Resource resource = new FileSystemResource(inputFile);
	
		JacksonJsonObjectReader<Employee> jsonObjectReader = new JacksonJsonObjectReader<>(Employee.class);
		jsonObjectReader.setMapper(new ObjectMapper());

		return new JsonItemReaderBuilder<Employee>()
				.name("EmployeeJsonReader")
				.jsonObjectReader(jsonObjectReader)
				.resource(resource)
				.build();
	}
	
	@Bean
	@StepScope
	public ItemWriter<Employee> writer() {
		return (c) -> c.forEach(System.out::println);
	}
	
	@Bean
	public Job serviceTriggerJob() {
		return jobFactory
				.get("ServiceTriggerJob")
				.flow(serviceTriggerStep())
				.end()
				.build();
	}
	
	@Bean
	public Step serviceTriggerStep() {
		return stepFactory
				.get("JsonReadStep")
				.<Employee, Employee>chunk(3)
				.reader(reader(null))		//use null when late-bound
				.writer(writer())
				.build();
				
	}
}
