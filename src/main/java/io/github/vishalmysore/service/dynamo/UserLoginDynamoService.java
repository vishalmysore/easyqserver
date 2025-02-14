package io.github.vishalmysore.service.dynamo;

import io.github.vishalmysore.service.base.UserLoginDBSrvice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service("userLoginDynamoService")

public class UserLoginDynamoService extends AWSDynamoService implements UserLoginDBSrvice {

    @Override
    public boolean updateUser(String userId) {
        try {
            // Step 1: Check if the user exists
            GetItemRequest getItemRequest = GetItemRequest.builder()
                    .tableName(LOGINUSER_TABLE_NAME)
                    .key(Map.of(
                            "userId", AttributeValue.builder().s(userId).build()
                    ))
                    .build();

            GetItemResponse getItemResponse = getDynamoDbClient().getItem(getItemRequest);

            if (getItemResponse.hasItem()) {
                // Step 2: User exists, proceed with updating the user data
                log.info("User exists. Updating user to verified.");

                Map<String, AttributeValueUpdate> updates = new HashMap<>();

                // Set tempUser to false
                updates.put("tempUser", AttributeValueUpdate.builder()
                        .value(AttributeValue.builder().bool(false).build())
                        .action(AttributeAction.PUT)
                        .build());

                // Set verified to true
                updates.put("verified", AttributeValueUpdate.builder()
                        .value(AttributeValue.builder().bool(true).build())
                        .action(AttributeAction.PUT)
                        .build());

                // Add the verifiedOnTimestamp
                updates.put("verifiedOnTimestamp", AttributeValueUpdate.builder()
                        .value(AttributeValue.builder().s(Instant.now().toString()).build())
                        .action(AttributeAction.PUT)
                        .build());

                // Perform the update
                UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                        .tableName(LOGINUSER_TABLE_NAME)
                        .key(Map.of(
                                "userId", AttributeValue.builder().s(userId).build()
                        ))
                        .attributeUpdates(updates)
                        .build();

                dynamoDbClient.updateItem(updateItemRequest);
                log.info("User updated successfully. TempUser set to false and Verified set to true.");
                return true;
            } else {
                // If user does not exist
                log.warn("User not found. Cannot update. UserID: {}", userId);
                return false;
            }
        } catch (DynamoDbException e) {
            log.error("Error updating user after verification: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean createTempUser(String userId, String emailId, String ipAddress) {
        try {
            // Step 1: Check if the user exists
            GetItemRequest getItemRequest = GetItemRequest.builder()
                    .tableName(LOGINUSER_TABLE_NAME)
                    .key(Map.of(
                            "userId", AttributeValue.builder().s(userId).build()
                    ))
                    .build();

            GetItemResponse getItemResponse = getDynamoDbClient().getItem(getItemRequest);

            if (getItemResponse.hasItem()) {
                // Step 2: User exists, update lastLoggedInTimestamp
                log.info("Invalid Username please choose another. {} ",userId);


                return false;
            } else {
                // Step 3: User does not exist, create a new entry
                log.info("User does not exist. Creating a new record. {} ",userId);

                Map<String, AttributeValue> item = new HashMap<>();
                item.put("userId", AttributeValue.builder().s(userId).build());
                item.put("emailId", AttributeValue.builder().s(emailId).build());
                item.put("tempUser", AttributeValue.builder().bool(true).build());
                item.put("verified", AttributeValue.builder().bool(false).build());
                item.put("createdTimestamp", AttributeValue.builder().s(Instant.now().toString()).build());
                item.put("ipAddress", AttributeValue.builder().s(ipAddress).build());
                item.put("lastLoggedInTimestamp", AttributeValue.builder().s(Instant.now().toString()).build());

                PutItemRequest putItemRequest = PutItemRequest.builder()
                        .tableName(LOGINUSER_TABLE_NAME)
                        .item(item)
                        .build();

                dynamoDbClient.putItem(putItemRequest);
                log.info("User created successfully.");
                return true;

            }
        } catch (DynamoDbException e) {
            log.error("Error updating user login: " + e.getMessage());
            return false;
        }

    }


    @Override
    @Async
    public void trackUserLogin(String userId, String emailId, String ipAddress) {
        try {
            // Step 1: Check if the user exists
            GetItemRequest getItemRequest = GetItemRequest.builder()
                    .tableName(LOGINUSER_TABLE_NAME)
                    .key(Map.of(
                            "userId", AttributeValue.builder().s(userId).build()
                    ))
                    .build();

            GetItemResponse getItemResponse = getDynamoDbClient().getItem(getItemRequest);

            if (getItemResponse.hasItem()) {
                // Step 2: User exists, update lastLoggedInTimestamp
                log.info("User exists. Updating lastLoggedInTimestamp.");

                Map<String, AttributeValueUpdate> updates = new HashMap<>();
                updates.put("lastLoggedInTimestamp", AttributeValueUpdate.builder()
                        .value(AttributeValue.builder().s(Instant.now().toString()).build())
                        .action(AttributeAction.PUT)
                        .build());

                UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                        .tableName(LOGINUSER_TABLE_NAME)
                        .key(Map.of(
                                "userId", AttributeValue.builder().s(userId).build()
                        ))
                        .attributeUpdates(updates)
                        .build();

                dynamoDbClient.updateItem(updateItemRequest);
                log.info("User login time updated successfully.");
            } else {
                // Step 3: User does not exist, create a new entry
                log.info("User does not exist. Creating a new record.");

                Map<String, AttributeValue> item = new HashMap<>();
                item.put("userId", AttributeValue.builder().s(userId).build());
                item.put("emailId", AttributeValue.builder().s(emailId).build());
                item.put("createdTimestamp", AttributeValue.builder().s(Instant.now().toString()).build());
                item.put("ipAddress", AttributeValue.builder().s(ipAddress).build());
                item.put("lastLoggedInTimestamp", AttributeValue.builder().s(Instant.now().toString()).build());

                PutItemRequest putItemRequest = PutItemRequest.builder()
                        .tableName(LOGINUSER_TABLE_NAME)
                        .item(item)
                        .build();

                dynamoDbClient.putItem(putItemRequest);
                log.info("User created successfully.");

            }
        } catch (DynamoDbException e) {
            log.error("Error updating user login: " + e.getMessage());
        }
    }

    @Override
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

}
