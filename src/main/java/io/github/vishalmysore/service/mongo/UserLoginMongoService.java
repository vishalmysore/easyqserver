package io.github.vishalmysore.service.mongo;

import io.github.vishalmysore.service.base.UserLoginDBSrvice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service("userLoginMongoService")
public class UserLoginMongoService extends MongoService implements UserLoginDBSrvice {

    public CompletableFuture<Integer> insertUsageData(String restCallId, String ipAddress, String timestamp) {
        log.info("Inserting usage data for restCallId: " + restCallId + " ipAddress: " + ipAddress + " timestamp: " + timestamp);
        return null;
    }

    @Override
    public boolean updateUser(String userId) {
        log.info("Updating user: " + userId);
        return true;
    }

    @Override
    public boolean createTempUser(String userId, String emailId, String ipAddress) {
        log.info("Creating temp user: " + userId + " emailId: " + emailId + " ipAddress: " + ipAddress);
        return true;
    }

    @Override
    public void trackUserLogin(String userId, String emailId, String ipAddress) {
        log.info("Tracking user login: " + userId + " emailId: " + emailId + " ipAddress: " + ipAddress);
    }
}
