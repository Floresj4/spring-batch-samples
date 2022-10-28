package com.flores.development.springbatch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
public class CustomRecordParsingConfig {

	private JobBuilderFactory jobBuilder;
	
	private StepBuilderFactory stepBuilder;

	//this would be externalized in an ideal scenario
	private static final int CHUNK_SIZE = 10;
	
	@Bean
	@StepScope
	public Step customRecordParsingStep() {
		log.debug("Initializing custom record parsing step");
		
		return stepBuilder.get("CustomRecordParsingStep")
				.chunk(CHUNK_SIZE)
				.build();
	}
	
	@Bean
	@StepScope
	public Job customRecordParsingJob() {
		log.debug("Initializing Custom Record Parsing job...");
		
		return jobBuilder.get("CustomRecordParsingJob")
				.flow(customRecordParsingStep())
				.end()
				.build();
	}
}
