package io.github.vishalmysore.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.model.*;

@Log
@Service("quizResultsDynamoService")
public class QuizResultsDynamoService extends AWSDynamoService {

    protected static final String QRESULT_TABLE_NAME = "quizresults";
    @PostConstruct
    public void init() {
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
            if (listTablesResponse.tableNames().contains(QRESULT_TABLE_NAME)) {
                log.info("Table 'links' already exists. Skipping creation.");
                return;
            }

            // Define the table schema if not found
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName(QRESULT_TABLE_NAME)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("userId")
                                    .keyType(KeyType.HASH)  // Partition key
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("userId")
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
                                                    .attributeName("userId")
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
            System.out.println("Table created: " + createTableResponse.tableDescription().tableName());
            log.info("Waiting for Table "+QRESULT_TABLE_NAME+" to be created in region: " + REGION.id());
            waitForTableToBecomeActive(dynamoDbClient,QRESULT_TABLE_NAME);
            log.info("Table "+QRESULT_TABLE_NAME+" created successfully in region: " + REGION.id());
            log.info("Table description: " + createTableResponse.tableDescription().tableName());

        } catch (SdkException e) {
            log.severe("Error occurred while creating table: " + e.getMessage());
        }
    }
}
