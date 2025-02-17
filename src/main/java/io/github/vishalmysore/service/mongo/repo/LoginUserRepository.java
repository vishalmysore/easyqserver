package io.github.vishalmysore.service.mongo.repo;

import io.github.vishalmysore.data.mongo.LoginUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LoginUserRepository extends MongoRepository<LoginUser, String> {
    List<LoginUser> findByTempUserTrueAndVerifiedFalse();
    void deleteByUserIdIn(List<String> userIds);
    Optional<LoginUser> findByUserId(String userId);
}

