package com.flores.dev.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Autowired
	private JobBuilderFactory jobBuilder;
	
	@Autowired
	private StepBuilderFactory stepBuilder;
	
	private int CHUNK_SIZE = 3;

	/**
	 * Late bind the input file to initialize the reader.
	 * Currently using a pass-through line mapper
	 * @param inputFile from job parameters
	 * @return an item reader.
	 */
	@Bean
	@StepScope
	public ItemStreamReader<String> reader(@Value("#{jobParameters['inputFile']}") String inputFile) {
		Resource input = new FileSystemResource(inputFile);
		
		return new FlatFileItemReaderBuilder<String>()
				.name("itemReader1")
				.resource(input)
				.lineMapper(new PassThroughLineMapper())
				.build();
	}
	
	@Bean
	public Job job() {
		return jobBuilder.get("Job1")
				.flow(step())
				.end()
				.build();
	}
	
	@Bean
	public Step step() {
		final String STEP_NAME = "Step1";
		return stepBuilder.get(STEP_NAME)
				.<String, String>chunk(CHUNK_SIZE)
				.reader(reader(null))	//value will be set via late-binding
				.processor(processor())
				.writer(writer(null))	//value will be set via late-binding
				.listener(new AfterChunkListener())
				.build();
	}
	
	@Bean
	@StepScope
	public ItemProcessor<String, String> processor() {
		return (p) -> {
			log.info("\t" + p);
			return p;
		};
	}
	
	@Bean
	@StepScope
	public ItemStreamWriter<String> writer(@Value("#{jobParameters['outputFile']}") String outputFile) {
		Resource output = new FileSystemResource(outputFile);
		
		return new FlatFileItemWriterBuilder<String>()
				.name("itemWriter")
				.resource(output)
				.lineAggregator(new PassThroughLineAggregator<>())
				.build();
	}
	
	@Slf4j
	public static class AfterChunkListener {

		@AfterChunk
		public void afterChunk() {
			log.info("Chunk completed.");
		}
	}
}
