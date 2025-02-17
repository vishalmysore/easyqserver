package io.github.vishalmysore.service.dynamo;

import io.github.vishalmysore.chatter.EasyQNotificationHandler;
import io.github.vishalmysore.data.Link;
import io.github.vishalmysore.data.Score;
import io.github.vishalmysore.service.base.ArticleScoringDBService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service ("articleScoringDBService")
@ConditionalOnProperty(name = "easyQZ_DBTYPE", havingValue = "aws", matchIfMissing = true)
public class ArticleScoringDynamoService extends AWSDynamoService implements ArticleScoringDBService {
    protected static final String ARTICLESCORE_TABLE_NAME = "articlescores";


    @PostConstruct
    public void init() {
        super.init();
        createQuizResultTable();

    }
    private void createQuizResultTable() {
        try {
            if (dynamoDbClient == null) {
                log.error("DynamoDbClient is not initialized. Ensure that init() is called first.");
                return;
            }

            // Check if the table 'links' already exists
            ListTablesRequest listTablesRequest = ListTablesRequest.builder().build();
            ListTablesResponse listTablesResponse = dynamoDbClient.listTables(listTablesRequest);
            if (listTablesResponse.tableNames().contains(ARTICLESCORE_TABLE_NAME)) {
                log.info("Table "+ARTICLESCORE_TABLE_NAME+"already exists. Skipping creation.");
                return;
            }

            // Define the table schema if not found
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName(ARTICLESCORE_TABLE_NAME)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("linkId")
                                    .keyType(KeyType.HASH)  // Partition key
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("linkId")
                                    .attributeType(ScalarAttributeType.S)  // String type for id
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("totalScore")
                                    .attributeType(ScalarAttributeType.N)
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
                                    .indexName("totalScoreIndex")
                                    .keySchema(
                                            KeySchemaElement.builder()
                                                    .attributeName("linkId")
                                                    .keyType(KeyType.HASH)  // Partition key of the GSI
                                                    .build(),
                                            KeySchemaElement.builder()
                                                    .attributeName("totalScore")
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
            log.info("Waiting for Table "+ARTICLESCORE_TABLE_NAME+" to be created in region: " + REGION.id());
            waitForTableToBecomeActive(dynamoDbClient,ARTICLESCORE_TABLE_NAME);
            log.info("Table "+ARTICLESCORE_TABLE_NAME+" created successfully in region: " + REGION.id());
            log.info("Table description: " + createTableResponse.tableDescription().tableName());

        } catch (SdkException e) {
            log.error("Error occurred while creating table: " + e.getMessage());
        }
    }
    @Override
    @Async
    public void insertScore(Score score, EasyQNotificationHandler easyQNotificationHandler) {
        try {
            if (dynamoDbClient == null) {
                log.error("DynamoDbClient is not initialized. Ensure that init() is called first.");
                return;
            }
            String id = generateSHA256Hash(score.getUrl());
            // Create the item for the new score
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("linkId", AttributeValue.builder().s(id).build());
            item.put("userId", AttributeValue.builder().s(score.getUserId()).build());
            item.put("quizId", AttributeValue.builder().s(score.getQuizId()).build());
            item.put("score", AttributeValue.builder().n(String.valueOf(score.getScore())).build());
            item.put("totalQuestions", AttributeValue.builder().n(String.valueOf(score.getTotalQuestions())).build());
            item.put("correctAnswers", AttributeValue.builder().n(String.valueOf(score.getCorrectAnswers())).build());
            item.put("incorrectAnswers", AttributeValue.builder().n(String.valueOf(score.getIncorrectAnswers())).build());
            item.put("skippedQuestions", AttributeValue.builder().n(String.valueOf(score.getSkippedQuestions())).build());
            item.put("totalScore", AttributeValue.builder().n(String.valueOf(score.getTotalScore())).build());
            item.put("percentage", AttributeValue.builder().n(String.valueOf(score.getPercentage())).build());
            item.put("topics", AttributeValue.builder().s(score.getTopics()).build());
            item.put("url", AttributeValue.builder().s(score.getUrl()).build());
            item.put("lastUpdated", AttributeValue.builder().s(String.valueOf(System.currentTimeMillis())).build()); // Add timestamp

            // Insert the new item into DynamoDB
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(ARTICLESCORE_TABLE_NAME)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);
            log.info("New score inserted successfully for userId: " + score.getUserId() + " and quizId: " + score.getQuizId()+" with score: "+score.getScore()+"in table: "+ARTICLESCORE_TABLE_NAME);


        } catch (SdkException e) {
            log.error("Error occurred while inserting new score: " + e.getMessage());
        }
    }

    public Link getLinkByUrl(String url) {
        String id = generateSHA256Hash(url);
        log.info("Fetching link with id: {}", id);

        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("id", AttributeValue.builder().s(id).build()))
                .build();

        Map<String, AttributeValue> item = dynamoDbClient.getItem(getRequest).item();
        if (item == null || item.isEmpty()) {
            throw new RuntimeException("Link not found with id: " + id);
        }

        return mapToLink(item);
    }


}
