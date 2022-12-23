package com.flores.dev.springbatch.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.flores.dev.springbatch.model.Person;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchConfig {

	@Bean
	@StepScope
	public ItemStreamReader<Person> reader(@Value("#{jobParameters['inputFile]}") String inputFile) {
		log.info("Initializing input file {}", inputFile);
		
		Resource fileResource = new FileSystemResource(inputFile);
		
		return new FlatFileItemReaderBuilder<Person>()
				.name(inputFile)
				.resource(fileResource)
				.delimited()
				.names("firstName", "lastName")
				.fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {
					{ setTargetType(Person.class); }
				})
				.build();
	}
}
