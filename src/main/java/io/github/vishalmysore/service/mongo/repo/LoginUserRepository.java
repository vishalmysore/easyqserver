package io.github.vishalmysore.service.mongo.repo;

import io.github.vishalmysore.data.mongo.LoginUser;
import org.bson.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LoginUserRepository extends MongoRepository<LoginUser, String> {
    List<LoginUser> findByTempUserTrueAndVerifiedFalse();
    void deleteByUserIdIn(List<String> userIds);
    Optional<LoginUser> findByUserId(String userId);

    @Query(value = "{ 'userId': ?0 }", fields = "{ 'avatar': 1, '_id': 0 }")
    Optional<Document> findAvatarByUserId(String userId);

}

