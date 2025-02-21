package io.github.vishalmysore.service.mongo.repo;

import io.github.vishalmysore.data.UserPerformanceData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserPerformanceDataRepository extends MongoRepository<UserPerformanceData, String> {

    Optional<UserPerformanceData> findByUserId(String userId);
    void deleteByUserIdIn(List<String> userIds);
}