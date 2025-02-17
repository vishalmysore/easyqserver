package io.github.vishalmysore.service.dynamo;

import io.github.vishalmysore.data.ContactUs;
import io.github.vishalmysore.service.base.ContactUsDBService;
import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Log
@Service("contactUsDBService")
@ConditionalOnProperty(name = "easyQZ_DBTYPE", havingValue = "aws", matchIfMissing = true)
public class ContactUsDynamoService extends AWSDynamoService implements ContactUsDBService {
    protected static final String CONTACTUS_TABLE_NAME = "contactus";

    @PostConstruct
    public void init() {
        super.init();
        createQuizResultTable();

    }
    private void createQuizResultTable() {
        try {
            if (dynamoDbClient == null) {
                log.severe("DynamoDbClient is not initialized. Ensure that init() is called first.");
                return;
            }

            // Check if the table 'links' already exists
            ListTablesRequest listTablesRequest = ListTablesRequest.builder().build();
            ListTablesResponse listTablesResponse = dynamoDbClient.listTables(listTablesRequest);
            if (listTablesResponse.tableNames().contains(CONTACTUS_TABLE_NAME)) {
                log.info("Table "+CONTACTUS_TABLE_NAME+"already exists. Skipping creation.");
                return;
            }

            // Define the table schema if not found
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName(CONTACTUS_TABLE_NAME)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("emailId")
                                    .keyType(KeyType.HASH)  // Partition key
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("type")
                                    .attributeType(ScalarAttributeType.S)  // String type for id
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("emailId")
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
                                    .indexName("emailIdIndex")
                                    .keySchema(
                                            KeySchemaElement.builder()
                                                    .attributeName("type")
                                                    .keyType(KeyType.HASH)  // Partition key of the GSI
                                                    .build(),
                                            KeySchemaElement.builder()
                                                    .attributeName("emailId")
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
            log.info("Waiting for Table "+CONTACTUS_TABLE_NAME+" to be created in region: " + REGION.id());
            waitForTableToBecomeActive(dynamoDbClient,CONTACTUS_TABLE_NAME);
            log.info("Table "+CONTACTUS_TABLE_NAME+" created successfully in region: " + REGION.id());
            log.info("Table description: " + createTableResponse.tableDescription().tableName());

        } catch (SdkException e) {
            log.severe("Error occurred while creating table: " + e.getMessage());
        }
    }

    @Async
    public void insertSupportTicket(ContactUs score) {
        try {
            if (dynamoDbClient == null) {
                log.severe("DynamoDbClient is not initialized. Ensure that init() is called first.");
                return;
            }

            // Create the item for the new score
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("emailId", AttributeValue.builder().s(score.getEmail()).build());
            item.put("type", AttributeValue.builder().s(score.getType()).build());
            item.put("message", AttributeValue.builder().s(score.getMessage()).build());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTimestamp = LocalDateTime.now().format(formatter);

            item.put("createdTimestamp", AttributeValue.builder().s(formattedTimestamp).build());

            // Insert the new item into DynamoDB
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(CONTACTUS_TABLE_NAME)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);
          log.info("Support ticket ."+score);

        } catch (SdkException e) {
            log.severe("Error occurred while inserting new score: " + e.getMessage());
        }
    }
}
