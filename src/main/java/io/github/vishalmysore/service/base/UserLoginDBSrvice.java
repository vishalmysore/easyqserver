package io.github.vishalmysore.service.base;

import java.util.concurrent.CompletableFuture;

public interface UserLoginDBSrvice {

    public boolean makeUserPermanent(String userId, String emailId);
    public boolean createTempUser(String userId, String emailId, String ipAddress, String avatar);
    public void trackUserLogin(String userId, String emailId, String ipAddress);
    public CompletableFuture<Integer> insertUsageData(String restCallId, String ipAddress, String timestamp);

    public void recordUserLogout(String userId);

    public String getAvtaarByUserId(String userId);
}
