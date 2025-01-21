package com.flores.dev.springbatch.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.flores.dev.springbatch.model.WeightEntry;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
@EnableBatchProcessing
public class SpringDynamoConfig {

	@Value("${aws.endpoint}")
	private String DB_ENDPOINT;

	@Value("${aws.region}")
	private String AWS_REGION;

	@Value("${aws.accessKey}")
	private String AWS_ACCESS_KEY;

	@Value("${aws.secretKey}")
	private String AWS_SECRET_KEY;

	@Value("${aws.sessionToken}")
	private String SESSION_TOKEN;
    
	@Autowired
	JobBuilderFactory jobBuilder;
	
	@Autowired
	StepBuilderFactory stepBuilder;
	
	@Bean
	@StepScope
	public ItemStreamReader<WeightEntry> getReader(@Value("#{jobParameters['inputFile']}") String inputFile) {
		Resource resource = new FileSystemResource(inputFile);

		return new FlatFileItemReaderBuilder<WeightEntry>()
				.resource(resource)
				.delimited()
				.names("Date", "Value")
				.fieldSetMapper(new BeanWrapperFieldSetMapper<WeightEntry>() {{
					setTargetType(WeightEntry.class);
				}})
				.build();
	}
	
	@Bean
    private DynamoDbClient getDynamoDbClient() throws URISyntaxException {
		return DynamoDbClient.builder()
				.credentialsProvider(StaticCredentialsProvider
						.create(AwsBasicCredentials.create(AWS_ACCESS_KEY, 
								AWS_SECRET_KEY)))
				.region(Region.US_EAST_1)
				.endpointOverride(new URI(DB_ENDPOINT))
				.build();
    }
	
	@Bean
	@StepScope
	public ItemWriter<WeightEntry> getWriter(@Value("#{jobParameters['tableName']}") String tableName) throws URISyntaxException {
		return DynamoDbWriter.builder()
				.withDynamo(getDynamoDbClient())
				.withTableName(tableName)
				.build();
	}
}
