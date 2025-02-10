package io.github.vishalmysore.service;

import io.github.vishalmysore.chatter.EasyQScoreHandler;
import io.github.vishalmysore.data.QuizType;
import io.github.vishalmysore.data.Score;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("quizResultsDynamoService")
public class QuizResultsDynamoService extends AWSDynamoService {

    protected static final String USER_LATEST_SCORE = "user_score";

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
            if (listTablesResponse.tableNames().contains(USER_LATEST_SCORE)) {
                log.info("Table "+ USER_LATEST_SCORE +"already exists. Skipping creation.");
                return;
            }

            // Define the table schema if not found
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName(USER_LATEST_SCORE)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("userId")
                                    .keyType(KeyType.HASH)  // Partition key
                                    .build(),
                            KeySchemaElement.builder()
                                    .attributeName("timestamp")
                                    .keyType(KeyType.RANGE)  // Sort key
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("userId")
                                    .attributeType(ScalarAttributeType.S)  // String type for id
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("timestamp")
                                    .attributeType(ScalarAttributeType.N)  // String type for timestamp
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
                                    .indexName("timestampIndex")
                                    .keySchema(
                                            KeySchemaElement.builder()
                                                    .attributeName("userId")
                                                    .keyType(KeyType.HASH)  // Partition key of the GSI
                                                    .build(),
                                            KeySchemaElement.builder()
                                                    .attributeName("timestamp")
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
            log.info("Waiting for Table "+ USER_LATEST_SCORE +" to be created in region: " + REGION.id());
            waitForTableToBecomeActive(dynamoDbClient, USER_LATEST_SCORE);
            log.info("Table "+ USER_LATEST_SCORE +" created successfully in region: " + REGION.id());
            log.info("Table description: " + createTableResponse.tableDescription().tableName());

        } catch (SdkException e) {
            log.error("Error occurred while creating table: " + e.getMessage());
        }
    }

    @Async
    public void insertScore(Score score, EasyQScoreHandler easyQScoreHandler) {
        try {
            if (dynamoDbClient == null) {
                log.error("DynamoDbClient is not initialized. Ensure that init() is called first.");
                return;
            }

            // Create the item for the new score
            Map<String, AttributeValue> item = new HashMap<>();
            String id = "Story_Topic";
            if(score.getQuizType().equals(QuizType.LINK)) {
                id = generateSHA256Hash(score.getUrl());
                GetItemRequest getItemRequest = GetItemRequest.builder()
                        .tableName(TABLE_NAME)  // Your links table name
                        .key(Map.of(
                                "id", AttributeValue.builder().s(id).build()  // Assuming linkId is the primary key
                        ))
                        .build();

                // Retrieve the item from the links table
                GetItemResponse response = dynamoDbClient.getItem(getItemRequest);

                // Check if the item exists and get the 'keywords' (topics) if present
                String topics = null;
                if (response.item() != null && response.item().containsKey("keywords")) {
                    topics = response.item().get("keywords").s();  // Extract the 'keywords' from the response
                }

               // If topics are found, update the score object with the retrieved topics
                if (topics != null) {
                    item.put("latest_topics", AttributeValue.builder().s(topics).build());
                } else {
                    item.put("latest_topics", AttributeValue.builder().s("No topics found").build());
                }
                item.put("latest_linkId", AttributeValue.builder().s(id).build());
                item.put("latest_url", AttributeValue.builder().s(score.getUrl()).build());

            } else if(score.getQuizType().equals(QuizType.TOPIC)) {
                item.put("latest_topics", AttributeValue.builder().s(score.getTopics()).build());
                item.put("latest_linkId", AttributeValue.builder().s("NA").build());
                item.put("latest_url", AttributeValue.builder().s("NA").build());

            } else if(score.getQuizType().equals(QuizType.STORY)) {
                item.put("latest_topics", AttributeValue.builder().s("story").build());
                item.put("latest_linkId", AttributeValue.builder().s("NA").build());

            }
            item.put("latest_score_type", AttributeValue.builder().s(score.getQuizType().toString()).build());
            item.put("timestamp", AttributeValue.builder().n(String.valueOf(System.currentTimeMillis())).build());  // Add timestamp
            item.put("userId", AttributeValue.builder().s(score.getUserId()).build());
            item.put("quizId", AttributeValue.builder().s(score.getQuizId()).build());
            item.put("latest_score", AttributeValue.builder().n(String.valueOf(score.getScore())).build());
            item.put("latest_totalQuestions", AttributeValue.builder().n(String.valueOf(score.getTotalQuestions())).build());
            item.put("latest_correctAnswers", AttributeValue.builder().n(String.valueOf(score.getCorrectAnswers())).build());
            item.put("latest_incorrectAnswers", AttributeValue.builder().n(String.valueOf(score.getIncorrectAnswers())).build());
            item.put("latest_skippedQuestions", AttributeValue.builder().n(String.valueOf(score.getSkippedQuestions())).build());
            item.put("latest_totalScore", AttributeValue.builder().n(String.valueOf(score.getTotalScore())).build());
            item.put("latest_percentage", AttributeValue.builder().n(String.valueOf(score.getPercentage())).build());


            item.put("lastUpdated", AttributeValue.builder().s(String.valueOf(System.currentTimeMillis())).build()); // Add timestamp

            QueryRequest queryRequest = QueryRequest.builder()
                    .tableName(USER_LATEST_SCORE)
                    .keyConditionExpression("userId = :userId")  // Only query by userId
                    .expressionAttributeValues(Map.of(
                            ":userId", AttributeValue.builder().s(score.getUserId()).build()
                    ))
                    .scanIndexForward(false)  // Set to false to sort in descending order (latest first)
                    .limit(1)  // Limit to 1 result (latest)
                    .build();

            QueryResponse queryResponse = dynamoDbClient.query(queryRequest);
            int overallScore = 0;
// Extract the latest record
            if (!queryResponse.items().isEmpty()) {
                Map<String, AttributeValue> latestRecord = queryResponse.items().get(0);
                // Handle the latest record here
                overallScore = Integer.parseInt(latestRecord.get("overallScore").n()) + score.getTotalScore();
                log.info("User exists {} . Updating overall score.,{}",score.getUserId(),overallScore);
            } else {
                overallScore = score.getTotalScore();
                log.info("User does not exist. Creating a new record.,{}",score.getUserId());
            }




            item.put("overallScore", AttributeValue.builder().n(String.valueOf(overallScore)).build());
            // Insert the new item into DynamoDB
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(USER_LATEST_SCORE)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);
            log.info("New score inserted successfully for userId: " + score.getUserId() + " and quizId: " + score.getQuizId()+" with linkId: "+id+" in table "+ USER_LATEST_SCORE);

            easyQScoreHandler.sendScoreToUser(score.getUserId(), (double)overallScore);
        } catch (SdkException e) {
            e.printStackTrace();
            log.error("Error occurred while inserting new score: " + e.getMessage());
        }
    }

    public Double getOverallScore(String userId) {
        try {
            // Prepare the key to fetch the latest record for the user
            HashMap<String, AttributeValue> key = new HashMap<>();
            key.put("userId", AttributeValue.builder().s(userId).build());  // The partition key
            key.put("timestamp", AttributeValue.builder().n("0").build()); // Start with a low timestamp for the query

            // Query the table to get the latest record (reverse order)
            QueryRequest queryRequest = QueryRequest.builder()
                    .tableName(USER_LATEST_SCORE)  // Table name
                    .keyConditionExpression("userId = :userId")
                    .expressionAttributeValues(
                            Map.of(":userId", AttributeValue.builder().s(userId).build())
                    )
                    .scanIndexForward(false)  // This ensures the most recent entry comes first
                    .limit(1)  // Only fetch the latest record
                    .build();

            // Execute the query
            QueryResponse queryResponse = dynamoDbClient.query(queryRequest);

            if (queryResponse.items().isEmpty()) {
                log.warn("No records found for user: " + userId);
                return 0.0;  // No records found, return 0
            }

            // Get the latest record
            Map<String, AttributeValue> latestRecord = queryResponse.items().get(0);

            // Retrieve the "overallScore" from the record (ensure it's a number or can be parsed to Double)
            String overallScoreStr = latestRecord.get("overallScore").n();  // DynamoDB stores numbers as strings
            Double overallScore = Double.parseDouble(overallScoreStr);  // Parse string to Double

            // Return the final score
            log.info("Latest overall score for user " + userId + ": " + overallScore);
            return overallScore;

        } catch (SdkException e) {
            log.error("Error retrieving latest overall score for user " + userId + ": " + e.getMessage());
            return 0.0;  // Return default 0.0 in case of error
        }
    }
}
