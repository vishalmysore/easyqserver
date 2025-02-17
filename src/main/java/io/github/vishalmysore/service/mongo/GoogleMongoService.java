package io.github.vishalmysore.service.mongo;

import io.github.vishalmysore.data.GoogleUser;
import io.github.vishalmysore.service.base.GoogleDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service("googleDBService")
@Slf4j
@ConditionalOnProperty(name = "easyQZ_DBTYPE", havingValue = "mongo", matchIfMissing = true)
public class GoogleMongoService extends MongoService implements GoogleDBService {
    public static String LOGIN_TIME = "loginTime";
    public static String LOGOUT_TIME = "logoutTime";

    @Override
    public void insertGoogleUser(GoogleUser googleUser) {
        try {
            // Prepare the GoogleUser object to be inserted
            GoogleUser user = new GoogleUser();
            user.setEmail(googleUser.getEmail());
            user.setSub(googleUser.getSub());
            user.setName(googleUser.getName());
            user.setCreatedTimestamp(googleUser.getCreatedTimestamp());
            user.setEasyQZUserId(googleUser.getEasyQZUserId());
            user.setLoginTime(Instant.now().toString());  // Set the login timestamp

            // Insert the user into the MongoDB collection
            mongoTemplate.insert(user, GOOGLEUSERS_TABLE_NAME);
            log.info("Inserted GoogleUser with email: " + googleUser.getEmail());

        } catch (Exception e) {
            log.error("Error occurred while inserting GoogleUser: " + e.getMessage());
        }
    }
    @Override
    public void updateLoginAndLogoutTime(String email, String status) {
        try {
            // Get current time
            String currentTime = Instant.now().toString();
            Update update = new Update();

            // Set loginTime or logoutTime based on the status parameter
            if (LOGIN_TIME.equalsIgnoreCase(status)) {
                update.set("loginTime", currentTime);  // Set loginTime
            } else if (LOGOUT_TIME.equalsIgnoreCase(status)) {
                update.set("logoutTime", currentTime);  // Set logoutTime
            } else {
                log.error("Invalid status parameter: " + status);
                return;
            }

            // Create the query to find the user by email
            Query query = new Query(Criteria.where("email").is(email));

            // Update the item in the MongoDB collection
            mongoTemplate.updateFirst(query, update, GoogleUser.class, GOOGLEUSERS_TABLE_NAME);

            log.info("Updated " + status + "Time for user with email: " + email);

        } catch (Exception e) {
            log.error("Error occurred while updating " + status + "Time: " + e.getMessage());
        }
    }

    @Override
    public GoogleUser getGoogleUserByEmail(String email) {
        try {
            // Create a query to find the user by email
            Query query = new Query(Criteria.where("email").is(email));

            // Retrieve the user from the MongoDB collection
            GoogleUser googleUser = mongoTemplate.findOne(query, GoogleUser.class, GOOGLEUSERS_TABLE_NAME);

            if (googleUser != null) {
                log.info("User found with email: " + email);
                return googleUser; // Return the found user
            } else {
                log.info("No user found with email: " + email);
                return null; // Return null if no user is found
            }

        } catch (Exception e) {
            log.error("Error occurred while retrieving GoogleUser by email: " + e.getMessage());
            return null;
        }
    }
}
