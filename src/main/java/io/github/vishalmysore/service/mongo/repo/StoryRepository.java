package io.github.vishalmysore.service.mongo.repo;

import io.github.vishalmysore.data.Story;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StoryRepository extends MongoRepository<Story, String> {
    void deleteByUserIdIn(List<String> userIds);
}
