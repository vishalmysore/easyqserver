package io.github.vishalmysore.service.dynamo;

import io.github.vishalmysore.data.Story;
import io.github.vishalmysore.service.LLMService;
import io.github.vishalmysore.service.base.StoryDBService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("storyDBService")
@ConditionalOnProperty(name = "easyQZ_DBTYPE", havingValue = "aws", matchIfMissing = true)
public class StoryStorageService extends AWSDynamoService implements StoryDBService {
    protected static final String TABLE_NAME = "stories";

    @PostConstruct
    public void init() {
        super.init();
        createStoryTable();

    }

    public String createStory(String prompt, LLMService llm) {
        return  null;
    }
    private void createStoryTable() {
        try {
            if (dynamoDbClient == null) {
                log.error("DynamoDbClient is not initialized. Ensure that init() is called first.");
                return;
            }

            // Check if the table 'links' already exists
            ListTablesRequest listTablesRequest = ListTablesRequest.builder().build();
            ListTablesResponse listTablesResponse = dynamoDbClient.listTables(listTablesRequest);
            if (listTablesResponse.tableNames().contains(TABLE_NAME)) {
                log.info("Table "+TABLE_NAME+"already exists. Skipping creation.");
                return;
            }

            // Define the table schema if not found
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("storyId")
                                    .keyType(KeyType.HASH)  // Partition key
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("storyId")
                                    .attributeType(ScalarAttributeType.S)  // String type for id
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("storyType")
                                    .attributeType(ScalarAttributeType.S)  // String type for timestamp
                                    .build()
                    )
                    .provisionedThroughput(
                            ProvisionedThroughput.builder()
                                    .readCapacityUnits(5L)
                                    .writeCapacityUnits(5L)
                                    .build()
                    )
                    // Adding GSI for lastUsed
                    .globalSecondaryIndexes(
                            GlobalSecondaryIndex.builder()
                                    .indexName("storyTypeIndex")
                                    .keySchema(
                                            KeySchemaElement.builder()
                                                    .attributeName("storyId")
                                                    .keyType(KeyType.HASH)  // Partition key of the GSI
                                                    .build(),
                                            KeySchemaElement.builder()
                                                    .attributeName("storyType")
                                                    .keyType(KeyType.RANGE)  // Sort key of the GSI
                                                    .build()
                                    )
                                    .projection(Projection.builder()
                                            .projectionType(ProjectionType.ALL)  // Include all attributes in the index
                                            .build())
                                    .provisionedThroughput(
                                            ProvisionedThroughput.builder()
                                                    .readCapacityUnits(5L)
                                                    .writeCapacityUnits(5L)
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

// Create the table
            CreateTableResponse createTableResponse = dynamoDbClient.createTable(createTableRequest);
            log.info("Table created: " + createTableResponse.tableDescription().tableName());
            log.info("Waiting for Table "+TABLE_NAME+" to be created in region: " + REGION.id());
            waitForTableToBecomeActive(dynamoDbClient,TABLE_NAME);
            log.info("Table "+TABLE_NAME+" created successfully in region: " + REGION.id());
            log.info("Table description: " + createTableResponse.tableDescription().tableName());

        } catch (SdkException e) {
            log.error("Error occurred while creating table: " + e.getMessage());
        }
    }

    @Async
    public void insertStory(Story story) {
        try {
            if (dynamoDbClient == null) {
                log.error("DynamoDbClient is not initialized. Ensure that init() is called first.");
                return;
            }

            // Create the item for the new score
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("storyId", AttributeValue.builder().s(story.getStoryId()).build());
            item.put("storyType", AttributeValue.builder().s(story.getStoryType()).build());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTimestamp = LocalDateTime.now().format(formatter);
            item.put("userId", AttributeValue.builder().s(story.getUserId()).build());
            item.put("title", AttributeValue.builder().s(story.getTitle()).build());
            item.put("storyText", AttributeValue.builder().s(story.getStoryText()).build());
            item.put("createdTimestamp", AttributeValue.builder().s(formattedTimestamp).build());
            // Insert the new item into DynamoDB
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);
            log.info("New story inserted successfully for userId: " + story.getUserId() + " and storyId: " + story.getStoryId());

        } catch (SdkException e) {
            log.error("Error occurred while inserting new score: " + e.getMessage());
        }
    }
}
