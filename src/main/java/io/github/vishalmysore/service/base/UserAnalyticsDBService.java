package io.github.vishalmysore.service.base;

import io.github.vishalmysore.data.UserPerformance;

public interface UserAnalyticsDBService {
    UserPerformance getUserAnalytics(String userId);
}
