package io.github.vishalmysore.service.base;

import io.github.vishalmysore.data.UserPerformance;
import io.github.vishalmysore.data.UserPerformanceData;

public interface UserAnalyticsDBService {
    UserPerformance buildUserAnalytics(String userId);

    UserPerformance getUserAnalytics(String userId);
    void updateUserAnalytics(UserPerformance userPerformance);

    void updateUserPerformanceData(UserPerformanceData userPerformance);

     UserPerformanceData getUserPerformanceData(String userId);
}
