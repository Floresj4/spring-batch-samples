package development.springbatch.chunksteps;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Autowired
	private JobBuilderFactory jobBuilder;
	
	@Autowired
	private StepBuilderFactory stepBuilder;
	
	private int CHUNK_SIZE = 10;

	/**
	 * Late bind the input file to initialize the reader.
	 * Currently using a pass-through line mapper
	 * @param inputFile from job parameters
	 * @return an item reader.
	 */
	@Bean
	@StepScope
	private ItemReader<?> reader(@Value("#{inputFile}") Resource inputFile) {
		return new FlatFileItemReaderBuilder<String>()
				.name("itemReader1")
				.resource(inputFile)
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
				.chunk(CHUNK_SIZE)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
	}

	@Bean
	private ItemProcessor processor() {
		return null;
	}
	
	@Bean
	private ItemWriter<?> writer() {
		return null;
	}
}
