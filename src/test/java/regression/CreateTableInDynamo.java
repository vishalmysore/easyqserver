package regression;

import lombok.extern.java.Log;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Log
public class CreateTableInDynamo {
    private static String TABLE_NAME= "links";
    private static void waitForTableToBecomeActive(DynamoDbClient dynamoDbClient, String tableName) {
        while (true) {
            try {
                // Describe the table to get its current status
                DescribeTableRequest describeTableRequest = DescribeTableRequest.builder()
                        .tableName(tableName)
                        .build();
                DescribeTableResponse describeTableResponse = dynamoDbClient.describeTable(describeTableRequest);

                String tableStatus = describeTableResponse.table().tableStatusAsString();
                System.out.println("Table status: " + tableStatus);

                if (tableStatus.equals("ACTIVE")) {
                    System.out.println("Table is now ACTIVE.");
                    break;
                }

                // Wait for a while before checking again
                Thread.sleep(2000); // 2 seconds delay before retry

            } catch (DynamoDbException | InterruptedException e) {
                System.err.println("Error describing table: " + e.getMessage());
                break;
            }
        }
    }
    public static void main(String[] args) {
        // AWS Credentials
        String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");


        // Region
        Region region = Region.US_WEST_2; // Use your desired region

        // Create DynamoDbClient
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        try {
            // 1. Create a table
            CreateTableRequest createTableRequest1 = CreateTableRequest.builder()
                    .tableName("emp") // Table name
                    .keySchema(KeySchemaElement.builder()
                            .attributeName("UserId")
                            .keyType(KeyType.HASH) // Partition key
                            .build())
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("UserId")
                                    .attributeType(ScalarAttributeType.S)
                                    .build())
                    .provisionedThroughput(
                            ProvisionedThroughput.builder()
                                    .readCapacityUnits(5L)
                                    .writeCapacityUnits(5L)
                                    .build())
                    .build();

          //  CreateTableResponse createTableResponse = dynamoDbClient.createTable(createTableRequest);
           // System.out.println("Table created: " + createTableResponse.tableDescription().tableName());
          //  waitForTableToBecomeActive(dynamoDbClient,"emp"
         //   );
            // 2. Insert a new item using HashMap
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("UserId", AttributeValue.builder().s("123").build());
            item.put("Name", AttributeValue.builder().s("John Doe").build());
            item.put("Email", AttributeValue.builder().s("john.doe@example.com").build());

          //  PutItemRequest putItemRequest = PutItemRequest.builder()
           //         .tableName("emp")
           //        .item(item)
            //        .build();

          //  dynamoDbClient.putItem(putItemRequest);
            System.out.println("Item inserted successfully.");

                try {
                    if (dynamoDbClient == null) {
                        log.severe("DynamoDbClient is not initialized. Ensure that init() is called first.");
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
                    System.out.println("Table created: " + createTableResponse.tableDescription().tableName());
                    waitForTableToBecomeActive(dynamoDbClient, TABLE_NAME);

                    log.info("Table description: " + createTableResponse.tableDescription().tableName());
                    Map<String, AttributeValue> item1 = new HashMap<>();
                    item1.put("id", AttributeValue.builder().s("unique-id").build());
                    item1.put("url", AttributeValue.builder().s("https://example.com").build());
                    item1.put("author", AttributeValue.builder().s("John Doe").build());
                    item1.put("firstUsed", AttributeValue.builder().s(Instant.now().toString()).build());
                    item1.put("lastUsed", AttributeValue.builder().s(Instant.now().toString()).build());
                    item1.put("totalCount", AttributeValue.builder().n("1").build());

                    PutItemRequest putItemRequest = PutItemRequest.builder()
                            .tableName(TABLE_NAME)
                            .item(item1)
                            .build();

                    dynamoDbClient.putItem(putItemRequest);
                    System.out.println("Item inserted successfully.");

                    Map<String, AttributeValue> key = new HashMap<>();
                    key.put("id", AttributeValue.builder().s("unique-id").build());

                    GetItemRequest getRequest = GetItemRequest.builder()
                            .tableName(TABLE_NAME)
                            .key(key)
                            .build();

                   GetItemResponse getResponse = dynamoDbClient.getItem(getRequest);
                    System.out.println("Item retrieved: " + getResponse.item());

                    Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
                    String oneHourAgoString = oneHourAgo.toString();  // Format it as per your lastUsed format (ISO 8601)

// Perform the Scan operation to get items updated in the last 1 hour
                    ScanRequest scanRequest = ScanRequest.builder()
                            .tableName(TABLE_NAME)
                            .filterExpression("lastUsed >= :lastUsed")
                            .expressionAttributeValues(Map.of(
                                    ":lastUsed", AttributeValue.builder().s(oneHourAgoString).build()
                            ))
                            .build();

                    ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

// Process the scanned items
                    for (Map<String, AttributeValue> item2 : scanResponse.items()) {
                        System.out.println("Item updated in the last hour: " + item2);
                    }

                } catch (SdkException e) {
                    e.printStackTrace();
                }


        } catch (DynamoDbException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            // Close the DynamoDbClient
            dynamoDbClient.close();
        }
    }
}
