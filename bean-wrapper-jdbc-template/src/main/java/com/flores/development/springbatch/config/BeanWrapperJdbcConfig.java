package com.flores.development.springbatch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.flores.development.springbatch.PersonProcessor;
import com.flores.development.springbatch.model.Person;

@Configuration
@EnableBatchProcessing
public class BeanWrapperJdbcConfig {

	@Autowired
	private JobBuilderFactory jobBuilder;
	
	@Autowired
	private StepBuilderFactory stepBuilder;

	@Autowired
	private DataSource dataSource;
	
	@Bean
	@StepScope
	public FlatFileItemReader<Person> reader(@Value("#{jobParameters['inputFile']}") String inputFile) {
		
		Resource resource = new FileSystemResource(inputFile);
		
		return new FlatFileItemReaderBuilder<Person>()
				.resource(resource)
				.name("personReader")
				.delimited()
				.names(new String[] {"firstName", "lastName"})
			    .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
			        setTargetType(Person.class);
			      }})
				.build();
	}
	
	@Bean
	public PersonProcessor processor() {
		return new PersonProcessor();
	}
	
	@Bean
	public JdbcBatchItemWriter<Person> writer() {
		return new JdbcBatchItemWriterBuilder<Person>()
				.itemSqlParameterSourceProvider(
						new BeanPropertyItemSqlParameterSourceProvider<>())
				.sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
				.dataSource(dataSource)
				.build();
	}
	
	@Bean
	public Step step(ItemWriter<Person> writer) {
		return stepBuilder.get("step 1")
			.<Person, Person>chunk(10)
			.reader(reader(null))	//pass null for the late-bind parameter
			.processor(processor())
			.writer(writer())
			.build();
	}

	@Bean
	public Job importUserJob(JobCompletionNotificationListener listener, Step step) {
		return jobBuilder.get("importUserJob")
			.incrementer(new RunIdIncrementer())
			.listener(listener)
			.flow(step)
			.end()
			.build();			
	}
}
