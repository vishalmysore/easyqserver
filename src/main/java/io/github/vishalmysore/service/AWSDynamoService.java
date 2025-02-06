package io.github.vishalmysore.service;

import io.github.vishalmysore.data.Link;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service("awsDynamoService")
@Slf4j
public class AWSDynamoService {

    @Getter
    protected  DynamoDbClient dynamoDbClient;

    @Autowired
    protected LLMService llmService;

    // AWS Credentials
    protected  String accessKey;
    protected  String secretKey;
    protected  String TABLE_NAME ="links";
    protected  String USAGE_TABLE_NAME ="usage"; //should hold the rest calls and the ipaddress and time of the call
    protected  String LOGINUSER_TABLE_NAME ="loginuser";
    // AWS Region (Set this to the region you're using)
    protected  final Region REGION = Region.US_EAST_1;

    // Initialize AWS credentials and DynamoDbClient
    @PostConstruct
    public void init() {
        if (dynamoDbClient == null) {
            // Fetch AWS credentials from environment variables once during application startup
            accessKey = System.getenv("AWS_ACCESS_KEY_ID");
            secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");

            // Ensure credentials are available
            if (accessKey == null || secretKey == null) {
                log.error("AWS credentials not found in environment variables.");
                return;
            }

            // Create DynamoDbClient only once
            dynamoDbClient = DynamoDbClient.builder()
                    .region(REGION)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .build();

            log.info("DynamoDbClient initialized for region: " + REGION.id());
            createLinksTable();
            createUsageTable();
            createLoginUser();
        }
    }


    @Async
    public CompletableFuture<Integer> insertUsageData(String restCallId, String ipAddress, String timestamp) {
        try {
            // Check if the item exists
            GetItemRequest getItemRequest = GetItemRequest.builder()
                    .tableName(USAGE_TABLE_NAME)
                    .key(Map.of("restCallId", AttributeValue.builder().s(restCallId).build()))
                    .build();

            GetItemResponse getItemResponse = dynamoDbClient.getItem(getItemRequest);

            int totalUsed;
            if (getItemResponse.hasItem()) {
                // Item exists, update totalUsed
                totalUsed = Integer.parseInt(getItemResponse.item().get("totalUsed").n()) + 1;

                UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                        .tableName(USAGE_TABLE_NAME)
                        .key(Map.of("restCallId", AttributeValue.builder().s(restCallId).build()))
                        .updateExpression("SET totalUsed = :newTotal, ipaddress = :ip")
                        .expressionAttributeValues(Map.of(
                                ":newTotal", AttributeValue.builder().n(String.valueOf(totalUsed)).build(),
                                ":ip", AttributeValue.builder().s(ipAddress).build()

                        ))
                        .build();

                dynamoDbClient.updateItem(updateItemRequest);
                log.info("Updated totalUsed count to {} for restCallId {}", totalUsed, restCallId);
            } else {
                // Item does not exist, insert new entry with totalUsed = 1
                totalUsed = 1;
                Map<String, AttributeValue> item = new HashMap<>();
                item.put("restCallId", AttributeValue.builder().s(restCallId).build());
                item.put("ipaddress", AttributeValue.builder().s(ipAddress).build());
                item.put("totalUsed", AttributeValue.builder().n("1").build());
                item.put("timestamp", AttributeValue.builder().s(timestamp).build());
                PutItemRequest putItemRequest = PutItemRequest.builder()
                        .tableName(USAGE_TABLE_NAME)
                        .item(item)
                        .build();

                dynamoDbClient.putItem(putItemRequest);
                log.info("Inserted new entry with totalUsed = 1 for restCallId {}", restCallId);
            }

            return CompletableFuture.completedFuture(totalUsed);
        } catch (DynamoDbException e) {
            log.error("Failed to insert/update data in 'usage' table: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
    @Async
    public void saveOrUpdateLink(String url, String data) {
        String id = generateSHA256Hash(url); // Unique ID based on URL


        // Check if the link already exists
        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("id", AttributeValue.builder().s(id).build()))
                .build();

        GetItemResponse getResponse = dynamoDbClient.getItem(getRequest);

        if (getResponse.hasItem()) {
            // Update the existing record
            updateLink(id);
        } else {
            String author = llmService.callLLM(" who is the author of this article just provide the name and nothing else, if you cannot find the name just return unknown "+data);
            String keywords = llmService.callLLM(" what topic does this cover giving comma 5 separated topics for example java,programming  "+data);
            // Insert new record
            createNewLink(id, url, author, data,keywords);
        }
    }

    /**
     * Creates a new link entry in DynamoDB.
     */
    private void createNewLink(String id, String url, String author, String data,String keywords) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(id).build());
        item.put("url", AttributeValue.builder().s(url).build());
        item.put("firstUsed", AttributeValue.builder().s(Instant.now().toString()).build());
        item.put("lastUsed", AttributeValue.builder().s(Instant.now().toString()).build());
        item.put("totalAccessCount", AttributeValue.builder().n("1").build());
        item.put("author", AttributeValue.builder().s(author).build());
        item.put("data", AttributeValue.builder().s(data).build());
        item.put("keywords", AttributeValue.builder().s(keywords).build());
        PutItemRequest putRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();

        dynamoDbClient.putItem(putRequest);
        log.info("New link added: " + url);
    }

    /**
     * Updates the lastUsed timestamp and increments totalAccessCount.
     */
    private void updateLink(String id) {
        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("id", AttributeValue.builder().s(id).build()))
                .updateExpression("SET lastUsed = :lastUsed, totalAccessCount = totalAccessCount + :inc")
                .expressionAttributeValues(Map.of(
                        ":lastUsed", AttributeValue.builder().s(Instant.now().toString()).build(),
                        ":inc", AttributeValue.builder().n("1").build()
                ))
                .build();

        dynamoDbClient.updateItem(updateRequest);
        log.info("Updated link with id: " + id);
    }

    protected String generateSHA256Hash(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(url.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }
    protected  void waitForTableToBecomeActive(DynamoDbClient dynamoDbClient, String tableName) {
        while (true) {
            try {
                // Describe the table to get its current status
                DescribeTableRequest describeTableRequest = DescribeTableRequest.builder()
                        .tableName(tableName)
                        .build();
                DescribeTableResponse describeTableResponse = dynamoDbClient.describeTable(describeTableRequest);

                String tableStatus = describeTableResponse.table().tableStatusAsString();
                log.info("Table status: " + tableStatus);

                if (tableStatus.equals("ACTIVE")) {
                    log.info("Table is now ACTIVE. "+tableName);
                    break;
                }

                // Wait for a while before checking again
                Thread.sleep(2000); // 2 seconds delay before retry

            } catch (DynamoDbException | InterruptedException e) {
                log.info("Error describing table: " + e.getMessage());
                break;
            }
        }
    }
    // Method to create the 'links' table in DynamoDB
    private void createLinksTable() {
        try {
            if (dynamoDbClient == null) {
                log.error("DynamoDbClient is not initialized. Ensure that init() is called first.");
                return;
            }

            // Check if the table 'links' already exists
            ListTablesRequest listTablesRequest = ListTablesRequest.builder().build();
            ListTablesResponse listTablesResponse = dynamoDbClient.listTables(listTablesRequest);
            if (listTablesResponse.tableNames().contains(TABLE_NAME)) {
                log.info("Table 'links' already exists. Skipping creation.");
                return;
            }

            // Define the table schema if not found
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("id")
                                    .keyType(KeyType.HASH)  // Partition key
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("id")
                                    .attributeType(ScalarAttributeType.S)  // String type for id
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("lastUsed")
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
                                    .indexName("LastUsedIndex")
                                    .keySchema(
                                            KeySchemaElement.builder()
                                                    .attributeName("lastUsed")
                                                    .keyType(KeyType.HASH)  // Partition key of the GSI
                                                    .build(),
                                            KeySchemaElement.builder()
                                                    .attributeName("id")
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
            log.info("Waiting for Table 'links' to be created in region: " + REGION.id());
            waitForTableToBecomeActive(dynamoDbClient,TABLE_NAME);
            log.info("Table 'links' created successfully in region: " + REGION.id());
            log.info("Table description: " + createTableResponse.tableDescription().tableName());

        } catch (SdkException e) {
            log.error("Error occurred while creating table: " + e.getMessage());
        }
    }

    private void createUsageTable() {
        try {
            if (dynamoDbClient == null) {
                log.error("DynamoDbClient is not initialized. Ensure that init() is called first.");
                return;
            }

            // Check if the table 'links' already exists
            ListTablesRequest listTablesRequest = ListTablesRequest.builder().build();
            ListTablesResponse listTablesResponse = dynamoDbClient.listTables(listTablesRequest);
            if (listTablesResponse.tableNames().contains(USAGE_TABLE_NAME)) {
                log.info("Table 'links' already exists. Skipping creation.");
                return;
            }

            // Define the table schema if not found
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName(USAGE_TABLE_NAME)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("restCallId")
                                    .keyType(KeyType.HASH)  // Partition key
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("restCallId")
                                    .attributeType(ScalarAttributeType.S)  // String type for id
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("ipaddress")
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
                                    .indexName("ipaddressIndex")
                                    .keySchema(
                                            KeySchemaElement.builder()
                                                    .attributeName("ipaddress")
                                                    .keyType(KeyType.HASH)  // Partition key of the GSI
                                                    .build(),
                                            KeySchemaElement.builder()
                                                    .attributeName("restCallId")
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
            log.info("Waiting for Table 'usage' to be created in region: " + REGION.id());
            waitForTableToBecomeActive(dynamoDbClient,USAGE_TABLE_NAME);
            log.info("Table 'usage' created successfully in region: " + REGION.id());
            log.info("Table description: " + createTableResponse.tableDescription().tableName());

        } catch (SdkException e) {
            log.error("Error occurred while creating table: " + e.getMessage());
        }
    }

    private void createLoginUser() {
        try {
            if (dynamoDbClient == null) {
                log.error("DynamoDbClient is not initialized. Ensure that init() is called first.");
                return;
            }

            // Check if the table 'links' already exists
            ListTablesRequest listTablesRequest = ListTablesRequest.builder().build();
            ListTablesResponse listTablesResponse = dynamoDbClient.listTables(listTablesRequest);
            if (listTablesResponse.tableNames().contains(LOGINUSER_TABLE_NAME)) {
                log.info("Table 'links' already exists. Skipping creation.");
                return;
            }

            // Define the table schema if not found
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName(LOGINUSER_TABLE_NAME)
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
            log.info("Table created: " + createTableResponse.tableDescription().tableName());
            log.info("Waiting for Table "+LOGINUSER_TABLE_NAME+" to be created in region: " + REGION.id());
            waitForTableToBecomeActive(dynamoDbClient,LOGINUSER_TABLE_NAME);
            log.info("Table "+LOGINUSER_TABLE_NAME+" created successfully in region: " + REGION.id());
            log.info("Table description: " + createTableResponse.tableDescription().tableName());

        } catch (SdkException e) {
            log.error("Error occurred while creating table: " + e.getMessage());
        }
    }

    public List<Link> getTrendingArticlesInLastHour() {
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        String oneHourAgoString = oneHourAgo.toString();  // Format it as per your lastUsed format (ISO 8601)
        // Get the current timestamp and subtract 1 hour
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("lastUsed >= :lastUsed")
                .expressionAttributeValues(Map.of(
                        ":lastUsed", AttributeValue.builder().s(oneHourAgoString).build()
                ))
                .build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);
        List<Link> trendingArticles = new ArrayList<>();
// Process the scanned items
        for (Map<String, AttributeValue> item : scanResponse.items()) {
            String url = item.get("url").s();
            String author = item.get("author").s();
            int totalAccessCount = Integer.parseInt(item.get("totalAccessCount").n());

            // Add the Article to the list
            trendingArticles.add(new Link(url, author, totalAccessCount));
        }





        // Return the list of trending articles
        return trendingArticles;
    }

    public List<Link> getAllTimeTrendingArticles() {
        // Scan all items in the table
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("totalAccessCount > :count")
                .expressionAttributeValues(Map.of(
                        ":count", AttributeValue.builder().n("0").build()  // Only items with non-zero access count
                ))
                .build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

// Process the scanned items and sort them by totalAccessCount
        List<Map<String, AttributeValue>> trendingArticles = scanResponse.items();

// Sort articles by totalAccessCount (descending order)
        trendingArticles.sort((a, b) -> {
            int countA = Integer.parseInt(a.get("totalAccessCount").n());
            int countB = Integer.parseInt(b.get("totalAccessCount").n());
            return Integer.compare(countB, countA);  // Sort descending
        });

        List<Link> trendingArticlesLink = new ArrayList<>();
// Print the top 10 all-time trending articles
        for (int i = 0; i < Math.min(10, trendingArticles.size()); i++) {
            Map<String, AttributeValue> article = trendingArticles.get(i);
            String url = article.get("url").s();
            String author = article.get("author").s();
            int totalAccessCount = Integer.parseInt(article.get("totalAccessCount").n());

            // Add the Article to the list
            trendingArticlesLink.add(new Link(url, author, totalAccessCount));
        }
        return trendingArticlesLink;
    }


}
