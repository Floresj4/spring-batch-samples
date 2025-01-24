package com.flores.dev.springbatch.config;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.batch.item.ItemWriter;

import com.flores.dev.springbatch.model.WeightEntry;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ConsumedCapacity;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

@Slf4j
@Builder(setterPrefix = "with")
public class DynamoDbWriter implements ItemWriter<WeightEntry> {

	public static final String ATTRIBUTE_GUID = "guid";
	public static final String ATTRIBUTE_ENTRY_DATE = "entry-date";
	public static final String ATTRIBUTE_VALUE = "value";
	
	private String tableName;

	private boolean tableExists;
	
	private DynamoDbClient dynamoDbClient;
	
	private final Object lock = new Object();
	
	@Override
	public void write(List<? extends WeightEntry> items) throws Exception {
		log.info("Writing {} items to {} table", items.size(), tableName);
	
		if(!tableExists) {
			synchronized(lock) {
				tableExists = createTable(dynamoDbClient, tableName);
			}
		}
		
		BatchWriteItemRequest batchItemRequest = batchItemRequest(dynamoDbClient, tableName, items, "");
		BatchWriteItemResponse batchItemResponse = dynamoDbClient.batchWriteItem(batchItemRequest);
		List<ConsumedCapacity> consumedCapacity = batchItemResponse.consumedCapacity();

		String capacity = consumedCapacity.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(
						System.lineSeparator()));
		
		log.info("BatchWriteItemRequest complete.  Consumed capacity: {}", capacity);
	}
	
	public static Map<String, AttributeValue> getItemMap(String userGuid, WeightEntry entry) {
		Map<String, AttributeValue> item = new HashMap<>();

		AttributeValue guid = AttributeValue.builder()
				.s(userGuid)
				.build();
		
		item.put(ATTRIBUTE_GUID, guid);
		AttributeValue entryDateAttr = AttributeValue.builder()
				.s(entry.getDate())
				.build();

		item.put(ATTRIBUTE_ENTRY_DATE, entryDateAttr);
		AttributeValue valueAttr = AttributeValue.builder()
				.n(String.valueOf(entry.getValue()))
				.build();

		item.put(ATTRIBUTE_VALUE, valueAttr);
		return item;
	}
	
	public static BatchWriteItemRequest batchItemRequest(DynamoDbClient client, String tableName, List<? extends WeightEntry> entries, String userGuid) {
		Map<String, List<WriteRequest>> requestItems = new HashMap<>();
		
		log.info("Attempting to add {} entries", entries.size());
		
		for(int i = 0; i < entries.size(); i++) {			
			WeightEntry entry = entries.get(i);
			Map<String, AttributeValue> item = getItemMap(userGuid, entry);
			
			List<WriteRequest> putItems = requestItems.getOrDefault(tableName, new ArrayList<>());
			PutRequest put = PutRequest.builder()
					.item(item)
					.build();

			putItems.add(WriteRequest.builder()
					.putRequest(put)
					.build());
			
			requestItems.put(tableName, putItems);
		}
		
		return BatchWriteItemRequest.builder()
				.returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
				.requestItems(requestItems)
				.build();
	}
	
	public static boolean createTable(DynamoDbClient client, String tableName) {
		//describe request to check if exists and if created
		DescribeTableRequest describeTableRequest = DescribeTableRequest.builder()
				.tableName(tableName)
				.build();
		
		try {
			//check if the table exists first
			DescribeTableResponse describeTableResponse = client.describeTable(describeTableRequest);
			
			Instant creationDate = describeTableResponse.table().creationDateTime();
			log.info("{} table exists. Created {}", tableName, creationDate);
		}
		catch(ResourceNotFoundException e) {
			try{
				log.info("Table {} does not exist.  Creating...", tableName);
			
				CreateTableRequest createTableRequest = createWeightTableRequest();
				CreateTableResponse createTableResponse = client.createTable(createTableRequest);
				
				//wait until DynamoDb is finished
				DynamoDbWaiter waiter = client.waiter();
				WaiterResponse<DescribeTableResponse> waiterResponse = waiter.waitUntilTableExists(describeTableRequest);
				waiterResponse.matched()
						.response()
						.map(String::valueOf)
						.ifPresent(log::info);
				
				String newTableName = createTableResponse.tableDescription()
						.tableName();
	
				log.info("{} created.", newTableName);
			}
			catch(Exception ex) {
				log.error("An unknown exception occurred", ex);
				return false;
			}
		}
		
		return true;
	}
	
	public static CreateTableRequest createWeightTableRequest() {
		log.info("Creating Weight table request");

		//define table attributes
		AttributeDefinition guidAttribute = AttributeDefinition.builder()
				.attributeType(ScalarAttributeType.S)
				.attributeName(ATTRIBUTE_GUID)
				.build();
		
		AttributeDefinition dateAttribute = AttributeDefinition.builder()
				.attributeType(ScalarAttributeType.S)
				.attributeName(ATTRIBUTE_ENTRY_DATE)
				.build();

		//define table primary key attributes
		KeySchemaElement partitionKey = KeySchemaElement.builder()
				.keyType(KeyType.HASH)
				.attributeName(ATTRIBUTE_GUID)
				.build();
		
		KeySchemaElement sortKey = KeySchemaElement.builder()
				.keyType(KeyType.RANGE)
				.attributeName(ATTRIBUTE_ENTRY_DATE)
				.build();
		
		String tableName = "Weights";
		return CreateTableRequest.builder()
				.attributeDefinitions(guidAttribute, dateAttribute)
				.keySchema(partitionKey, sortKey)
				.provisionedThroughput(ProvisionedThroughput.builder()
						.readCapacityUnits(5L)
						.writeCapacityUnits(5L)
						.build())
				.tableName(tableName)
				.build();	
	}
}
