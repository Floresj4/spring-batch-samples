package development.springbatch.chunksteps;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Autowired
	private JobBuilderFactory jobBuilder;
	
	@Autowired
	private StepBuilderFactory stepBuilder;
	
	private int CHUNK_SIZE = 10;

	@Bean
	private ItemReader<?> reader() {
		return null;
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
