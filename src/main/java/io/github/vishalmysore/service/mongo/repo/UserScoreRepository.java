package io.github.vishalmysore.service.mongo.repo;


import io.github.vishalmysore.data.Score;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserScoreRepository extends MongoRepository<Score, String> {


    List<Score> findByUserIdOrderByTimestampDesc(String userId);
    void deleteByUserIdIn(List<String> userIds);
}
