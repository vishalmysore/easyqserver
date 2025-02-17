package io.github.vishalmysore.service.mongo.repo;

import io.github.vishalmysore.data.mongo.ArticleScore;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ArticleScoreRepository extends MongoRepository<ArticleScore, String> {
    void deleteByUserIdIn(List<String> userIds);
    List<ArticleScore> findByUserId(String userId);
}
