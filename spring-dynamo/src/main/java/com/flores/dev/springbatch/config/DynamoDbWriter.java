package com.flores.dev.springbatch.config;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

import com.flores.dev.springbatch.model.WeightEntry;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Slf4j
@Builder(setterPrefix = "with")
public class DynamoDbWriter implements ItemWriter<WeightEntry> {

	private String tableName;

	private DynamoDbClient dynamo;
	
	@Override
	public void write(List<? extends WeightEntry> items) throws Exception {
		log.info("Writing {} items to {} table", items.size(), tableName);
	}
}
