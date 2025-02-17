package io.github.vishalmysore.service.mongo;

import io.github.vishalmysore.data.UsageData;
import io.github.vishalmysore.service.base.UserLoginDBSrvice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service("userLoginDBService")
@ConditionalOnProperty(name = "easyQZ_DBTYPE", havingValue = "mongo", matchIfMissing = true)
public class UserLoginMongoService extends MongoService implements UserLoginDBSrvice {


    @Async
    @Override
    public CompletableFuture<Integer> insertUsageData(String restCallId, String ipAddress, String timestamp) {
        try {
            // Step 1: Check if the document exists
            Query query = new Query(Criteria.where("restCallId").is(restCallId));
            var existingUsage = mongoTemplate.findOne(query, UsageData.class, USAGE_COLLECTION_NAME);

            int totalUsed;
            if (existingUsage != null) {
                // Step 2: Document exists, update totalUsed count
                totalUsed = existingUsage.getTotalUsed() + 1;

                Update update = new Update()
                        .set("totalUsed", totalUsed)
                        .set("ipAddress", ipAddress);

                mongoTemplate.updateFirst(query, update, USAGE_COLLECTION_NAME);
                log.info("Updated totalUsed count to {} for restCallId {}", totalUsed, restCallId);
            } else {
                // Step 3: Document does not exist, create a new entry
                totalUsed = 1;
                UsageData newUsage = new UsageData(restCallId, ipAddress, totalUsed, timestamp);

                mongoTemplate.insert(newUsage, USAGE_COLLECTION_NAME);
                log.info("Inserted new entry with totalUsed = 1 for restCallId {}", restCallId);
            }

            return CompletableFuture.completedFuture(totalUsed);
        } catch (Exception e) {
            log.error("Failed to insert/update data in 'usage' collection: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public boolean makeUserPermanent(String userId,String emailId) {
        log.info("Updating user: " + userId);
        try {
            // Step 1: Check if the user exists
            Query query = new Query(Criteria.where("userId").is(userId));
            if (mongoTemplate.exists(query, LOGINUSER_TABLE_NAME)) {
                // Step 2: User exists, proceed with updating
                log.info("User exists. Updating user to verified.");

                Update update = new Update()
                        .set("tempUser", false)
                        .set("verified", true)
                        .set("emailId", emailId)
                        .set("verifiedOnTimestamp", Instant.now().toString());

                mongoTemplate.updateFirst(query, update, LOGINUSER_TABLE_NAME);

                log.info("User updated successfully. TempUser set to false and Verified set to true.");
                return true;
            } else {
                log.warn("User not found. Cannot update. UserID: {}", userId);
                return false;
            }
        } catch (Exception e) {
            log.error("Error updating user after verification: {}", e.getMessage());
            return false;
        }
     }



     @Override
    public boolean createTempUser(String userId, String emailId, String ipAddress,String avatar) {
        try {
            // Step 1: Check if the user exists
            Query query = new Query(Criteria.where("userId").is(userId));
            boolean userExists = mongoTemplate.exists(query, LOGINUSER_TABLE_NAME);

            if (userExists) {
                log.info("Invalid Username. Please choose another: {}", userId);
                return false;
            } else {
                // Step 2: User does not exist, create a new record
                log.info("User does not exist. Creating a new record: {}", userId);

                Map<String, Object> newUser = new HashMap<>();
                newUser.put("userId", userId);
                newUser.put("emailId", emailId);
                newUser.put("tempUser", true);
                newUser.put("verified", false);
                newUser.put("avatar", avatar);
                newUser.put("createdTimestamp", Instant.now().toString());
                newUser.put("ipAddress", ipAddress);
                newUser.put("lastLoggedInTimestamp", Instant.now().toString());

                mongoTemplate.insert(newUser, LOGINUSER_TABLE_NAME);

                log.info("User created successfully.");
                return true;
            }
        } catch (Exception e) {
            log.error("Error creating temp user: {}", e.getMessage());
            return false;
        }
    }

    @Async
    public void trackUserLogin(String userId, String emailId, String ipAddress) {
        try {
            // Step 1: Check if the user exists
            Query query = new Query(Criteria.where("userId").is(userId));
            boolean userExists = mongoTemplate.exists(query, LOGINUSER_TABLE_NAME);

            if (userExists) {
                // Step 2: User exists, update lastLoggedInTimestamp
                log.info("User exists. Updating lastLoggedInTimestamp.");

                Update update = new Update()
                        .set("lastLoggedInTimestamp", Instant.now().toString()).set("ipAddress", ipAddress);

                mongoTemplate.updateFirst(query, update, LOGINUSER_TABLE_NAME);

                log.info("User login time updated successfully.");
            } else {
                // Step 3: User does not exist, create a new record
                log.info("User does not exist. Will be Creating a new record.");
            }
        } catch (Exception e) {
            log.error("Error updating user login: {}", e.getMessage());
        }
    }

    @Override
    public void recordUserLogout(String userId) {
        try {
            // Step 1: Check if the user exists
            Query query = new Query(Criteria.where("userId").is(userId));
            boolean userExists = mongoTemplate.exists(query, LOGINUSER_TABLE_NAME);

            if (userExists) {
                // Step 2: User exists, update lastLoggedInTimestamp
                log.info("User exists. Updating lastLoggedOutTimestamp.");

                Update update = new Update()
                        .set("lastLoggedOutTimestamp", Instant.now().toString());

                mongoTemplate.updateFirst(query, update, LOGINUSER_TABLE_NAME);

                log.info("User logout time updated successfully.");
            } else {
                // Step 3: User does not exist, create a new record
                log.info("User does not exist. Cannot update logout time.");
            }
        } catch (Exception e) {
            log.error("Error updating user logout: {}", e.getMessage());
        }
    }
}
