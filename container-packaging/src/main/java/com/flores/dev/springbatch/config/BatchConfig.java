package com.flores.dev.springbatch.config;

import java.io.File;
import java.io.FileNotFoundException;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
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

	@Value("#{jobParameters['inputFile]}") 
	public String inputFile;
	
	@Bean
	@StepScope
	public ItemStreamReader<Person> reader() {
		log.info("Initializing input file {}", inputFile);
		
		Resource fileResource = new FileSystemResource(inputFile);
		
		return new FlatFileItemReaderBuilder<Person>()
				.name("BeanWrappedPersonReader")
				.resource(fileResource)
				.delimited()
				.names("firstName", "lastName")
				.fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {
					{ setTargetType(Person.class); }
				})
				.build();
	}
	
	@Bean
	@StepScope
	public ItemStreamWriter<Person> write() throws Exception {
		
		File outputFile = getOutputFile();
		Resource fileResource = new FileSystemResource(outputFile);

		return new FlatFileItemWriterBuilder<Person>()
				.name("PersonWriter")
				.resource(fileResource)
				.delimited()
				.fieldExtractor(new BeanWrapperFieldExtractor<Person>() {
					{ setNames(new String[]{"firstName", "lastName"}); }
				})
				.build();
	}
	
	private File getOutputFile() throws Exception {
		File input = new File(inputFile);
		if(!input.exists()) {
			throw new FileNotFoundException("Input file does not exist.");
		}
		
		if(input.isDirectory()) {
			throw new FileNotFoundException("Input file must be an file to execute.");
		}
		
		//will be a directory
		File parent = input.getParentFile();
		
		int separator = input.getName().indexOf('.');
		
		String name = input.getName();
		String extension = name.substring(separator + 1);
		String outName = name
				.substring(0, separator)
				.replace(extension, "-out." + extension);
		
		File outputFile = new File(parent, outName);
		
		log.info("Initializing output file as {}", outputFile.getCanonicalPath());
		return outputFile;
	}
}