package io.github.vishalmysore.service.mongo.repo;

import io.github.vishalmysore.data.UserPerformance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserAnalyticsRepository extends MongoRepository<UserPerformance, String> {
    // Add any custom queries if needed
    Optional<UserPerformance> findByUserId(String userId);
    void deleteByUserIdIn(List<String> userIds);
}
