package com.flores.dev.springbatch.config;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.flores.dev.springbatch.model.Employee;
import com.flores.dev.springbatch.reader.S3ObjectStreamReader;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Configuration
@EnableBatchProcessing
public class JsonReadConfig {

	@Autowired
	private JobBuilderFactory jobFactory;
	
	@Autowired
	private StepBuilderFactory stepFactory;

	@Autowired
	private JobRepository jobRepository;

	@Bean
	@StepScope
	public ItemStreamReader<Employee> reader(@Value("#{jobParameters['inputFile']}") String inputFile) {		
		return S3ObjectStreamReader.builder()
				.withClient(s3Client())
				.withResource(inputFile)
				.build();
	}

	@Bean
	public S3Client s3Client() {
		log.debug("Initializing S3 client bean...");
		DefaultCredentialsProvider provider = DefaultCredentialsProvider.builder()
				.build();
		
		return S3Client.builder()
				.region(Region.US_EAST_1)
				.credentialsProvider(provider)
				.forcePathStyle(true)
				.build();
	}

	@Bean
	SimpleJobLauncher getJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
	}
	
	@Bean
	@StepScope
	public ItemWriter<Employee> writer() {
		return (c) -> c.forEach(System.out::println);
	}
	
	@Bean
	public Job serviceTriggerJob() {
		return jobFactory.get("ServiceTriggerJob")
				.incrementer(new RunIdIncrementer())
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
