package com.flores.dev.springbatch.config;

import java.io.File;
import java.io.FileNotFoundException;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.flores.dev.springbatch.model.Person;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchConfig {
	
	@Autowired
	public JobRepository jobRepository;
	
	@Autowired
	public JobBuilderFactory jobBuilder;

	@Autowired
	private StepBuilderFactory stepBuilder;

	@Bean
	SimpleJobLauncher getJobLauncher() throws Exception {
		log.info("Initializing async job launcher...");

        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
	}
	
	@Bean
	@StepScope
	public ItemStreamReader<Person> reader(@Value("#{jobParameters['inputFile']}") String inputFile) {
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
	public ItemStreamWriter<Person> writer(@Value("#{jobParameters['inputFile']}") String inputFile) throws Exception {
		
		File outputFile = getOutputFile(inputFile);
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

	@Bean
	public Step batchProcessingStep() throws Exception {
		return stepBuilder
				.get("PersonProcessingStep")
				.<Person, Person>chunk(5)
				.reader(reader(null))
				.writer(writer(null))
				.build();
	}

	@Bean
	public Job batchProcessingJob() throws Exception {
		return jobBuilder
				.get("PersonProcessingJob")
				.flow(batchProcessingStep())
				.end()
				.build();
	}

	/**
	 * @return file to write based on the input file
	 * @throws Exception on initializing file
	 */
	private File getOutputFile(String inputFile) throws Exception {
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
		String outName = name.substring(0, separator);
		
		File outputFile = new File(parent, outName + "-out." + extension);
		
		log.info("Initializing output file as {}", outputFile.getCanonicalPath());
		return outputFile;
	}
}