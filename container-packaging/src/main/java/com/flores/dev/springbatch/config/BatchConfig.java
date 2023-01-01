package com.flores.dev.springbatch.config;

import java.io.File;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.flores.dev.springbatch.model.Person;
import com.flores.dev.springbatch.reader.S3ObjectStreamReader;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

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
	public S3Client getS3Client() {

		DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider
				.create();

		return S3Client.builder()
				.credentialsProvider(credentialsProvider)
				.region(Region.US_EAST_1)
				.build();
	}

	@Bean
	@StepScope
	public ItemStreamReader<Person> reader(@Value("#{jobParameters['inputFile']}") String inputFile) {
		log.info("Initializing input file {}", inputFile);
		
		return S3ObjectStreamReader.builder()
				.withResource(inputFile)
				.withClient(getS3Client())
				.build();
	}
	
	@Bean
	public ItemProcessor<Person, Person> personProcessor() {
		return (p) -> {
			log.debug(String.valueOf(p));
			return p;
		};
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
				.processor(personProcessor())
				.writer(writer(null))
				.build();
	}

	@Bean
	public Job batchProcessingJob() throws Exception {
		return jobBuilder
				.get("PersonProcessingJob")
				.incrementer(new RunIdIncrementer())
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
		
		int separator = input.getName().indexOf('.');
		
		String name = input.getName();
		String extension = name.substring(separator + 1);
		String outName = name.substring(0, separator);

		File outputFile = new File("../input-files/", outName + "-out." + extension);
		
		log.info("Initializing output file as {}", outputFile.getCanonicalPath());
		return outputFile;
	}
}