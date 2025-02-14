package io.github.vishalmysore.service.base;

import java.util.concurrent.CompletableFuture;

public interface UserLoginDBSrvice {

    public boolean updateUser(String userId);
    public boolean createTempUser(String userId, String emailId, String ipAddress);
    public void trackUserLogin(String userId, String emailId, String ipAddress);
    public CompletableFuture<Integer> insertUsageData(String restCallId, String ipAddress, String timestamp);
}
