package io.github.vishalmysore.service.mongo.repo;

import io.github.vishalmysore.data.mongo.DocumentLink;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface LinkRepository extends MongoRepository<DocumentLink, String> {
    List<DocumentLink> findByLastUsedBefore(Date lastUsed);
}
