package io.github.vishalmysore.service;

import lombok.extern.java.Log;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Log
@Service("userLoginDynamoService")

public class UserLoginDynamoService extends AWSDynamoService {




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
            log.severe("Error updating user login: " + e.getMessage());
        }
    }
}
