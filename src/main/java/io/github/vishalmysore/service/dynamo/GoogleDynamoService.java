package io.github.vishalmysore.service.dynamo;

import io.github.vishalmysore.data.GoogleUser;
import io.github.vishalmysore.service.base.GoogleDBService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service("googleDynamoService")
public class GoogleDynamoService extends AWSDynamoService implements GoogleDBService {
    protected static final String GOOGLEUSERS_TABLE_NAME = "google_users";
    public static String LOGIN_TIME = "loginTime";
    public static String LOGOUT_TIME = "logoutTime";
    @PostConstruct
    public void init() {
        super.init();
        createGoogleUserTable();
        insertGoogleUser(new GoogleUser("12345", "user@example.com", "John Doe", Instant.now().toString(),"helloUser"));  // Example usage with timestamp
    }

    private void createGoogleUserTable() {
        try {
            if (dynamoDbClient == null) {
                log.error("DynamoDbClient is not initialized. Ensure that init() is called first.");
                return;
            }

            // Check if the table 'google_users' already exists
            ListTablesRequest listTablesRequest = ListTablesRequest.builder().build();
            ListTablesResponse listTablesResponse = dynamoDbClient.listTables(listTablesRequest);
            if (listTablesResponse.tableNames().contains(GOOGLEUSERS_TABLE_NAME)) {
                log.info("Table " + GOOGLEUSERS_TABLE_NAME + " already exists. Skipping creation.");
                return;
            }

            // Define the table schema if not found
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName(GOOGLEUSERS_TABLE_NAME)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("email")  // Using email as the partition key
                                    .keyType(KeyType.HASH)  // Partition key
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("email")  // Email as the partition key
                                    .attributeType(ScalarAttributeType.S)  // String type for email
                                    .build()
                    )
                    .provisionedThroughput(
                            ProvisionedThroughput.builder()
                                    .readCapacityUnits(5L)
                                    .writeCapacityUnits(5L)
                                    .build()
                    )
                    .build();

            // Create the table
            CreateTableResponse createTableResponse = dynamoDbClient.createTable(createTableRequest);
            log.info("Table created: " + createTableResponse.tableDescription().tableName());
            log.info("Waiting for Table " + GOOGLEUSERS_TABLE_NAME + " to be created...");
            waitForTableToBecomeActive(dynamoDbClient, GOOGLEUSERS_TABLE_NAME);
            log.info("Table " + GOOGLEUSERS_TABLE_NAME + " created successfully.");

        } catch (SdkException e) {
            log.error("Error occurred while creating table: " + e.getMessage());
        }
    }

    // Method to insert a GoogleUser into the DynamoDB table
    public void insertGoogleUser(GoogleUser googleUser) {
        try {
            // Prepare the item to insert
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(GOOGLEUSERS_TABLE_NAME)
                    .item(Map.of(
                            "email", AttributeValue.builder().s(googleUser.getEmail()).build(),
                            "sub", AttributeValue.builder().s(googleUser.getSub()).build(),
                            "name", AttributeValue.builder().s(googleUser.getName()).build(),
                            "createdTimestamp", AttributeValue.builder().s(googleUser.getCreatedTimestamp()).build(),  // Add the createdTimestamp
                            "easyQZUserId", AttributeValue.builder().s(googleUser.getEasyQZUserId()).build(),
                            LOGIN_TIME,AttributeValue.builder().s(Instant.now().toString()).build()// Add easyQZUserId
                    ))
                    .build();

            // Insert the item into the DynamoDB table
            dynamoDbClient.putItem(putItemRequest);
            log.info("Inserted GoogleUser with email: " + googleUser.getEmail());

        } catch (SdkException e) {
            log.error("Error occurred while inserting GoogleUser: " + e.getMessage());
        }
    }

    public void updateLoginAndLogoutTime(String email, String status) {
        try {
            // Get current time
            String currentTime = Instant.now().toString();
            String loginTime = null;
            String logoutTime = null;

            // Set loginTime or logoutTime based on the status parameter
            if (LOGIN_TIME.equalsIgnoreCase(status)) {
                loginTime = currentTime;  // Set loginTime
            } else if (LOGOUT_TIME.equalsIgnoreCase(status)) {
                logoutTime = currentTime;  // Set logoutTime
            } else {
                log.error("Invalid status parameter: " + status);
                return;
            }

            // Create the update expression to update the loginTime and/or logoutTime
            UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                    .tableName(GOOGLEUSERS_TABLE_NAME)
                    .key(Map.of(
                            "email", AttributeValue.builder().s(email).build()  // Use email as the key
                    ))
                    .updateExpression("SET loginTime = :loginTime, logoutTime = :logoutTime")
                    .expressionAttributeValues(Map.of(
                            ":loginTime", loginTime == null ? AttributeValue.builder().nul(true).build() : AttributeValue.builder().s(loginTime).build(),
                            ":logoutTime", logoutTime == null ? AttributeValue.builder().nul(true).build() : AttributeValue.builder().s(logoutTime).build()
                    ))
                    .build();

            // Update the item in the DynamoDB table
            dynamoDbClient.updateItem(updateItemRequest);
            log.info("Updated " + status + "Time for user with email: " + email);

        } catch (SdkException e) {
            log.error("Error occurred while updating " + status + "Time: " + e.getMessage());
        }
    }
    // Method to retrieve a GoogleUser by email from the DynamoDB table
    public GoogleUser getGoogleUserByEmail(String email) {
        try {
            // Retrieve the item by email
            GetItemRequest getItemRequest = GetItemRequest.builder()
                    .tableName(GOOGLEUSERS_TABLE_NAME)
                    .key(Map.of(
                            "email", AttributeValue.builder().s(email).build()  // Use email as the key
                    ))
                    .build();

            // Get the item from the DynamoDB table
            GetItemResponse getItemResponse = dynamoDbClient.getItem(getItemRequest);

            if (getItemResponse.hasItem()) {
                // Extract the item attributes
                Map<String, AttributeValue> item = getItemResponse.item();
                String sub = item.get("sub").s();
                String name = item.get("name").s();
                String createdTimestamp = item.get("createdTimestamp").s();
                String easyQZUserId = item.get("easyQZUserId").s();
                // Create a GoogleUser object from the retrieved data
                return new GoogleUser(sub, email, name, createdTimestamp,easyQZUserId);
            } else {
                log.info("No user found with email: " + email);
                return null;
            }

        } catch (SdkException e) {
            log.error("Error occurred while retrieving GoogleUser by email: " + e.getMessage());
            return null;
        }
    }
}
