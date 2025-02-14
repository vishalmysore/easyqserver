package io.github.vishalmysore.service.mongo;

import io.github.vishalmysore.data.Link;
import io.github.vishalmysore.data.UsageData;
import io.github.vishalmysore.service.LLMService;
import io.github.vishalmysore.service.base.BaseDBService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service("mongoService")
public class MongoService implements BaseDBService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private LLMService llmService;
    @Value("${easyQZ_DBTYPE}")
    private String dbType;

    private static final String COLLECTION_NAME = "links";
    private static final String USAGE_COLLECTION_NAME = "usage"; // Holds usage data like rest calls and IP addresses
    protected  String LOGINUSER_TABLE_NAME ="loginuser";

    @PostConstruct
    @ConditionalOnProperty(name = "easyQZ_DBTYPE", havingValue = "mongo", matchIfMissing = true)
    public void init() {
        if (mongoTemplate == null) {
            log.error("MongoTemplate is not available. MongoService initialization failed.");
        } else if("mongo".equals(dbType)) {
            log.info("MongoService initialized with DB type: " + dbType);
            createCollectionsIfNotExist();
        }
    }

    private void createCollectionsIfNotExist() {
        // Create collections if they don't exist
        try {
            if (!mongoTemplate.collectionExists(COLLECTION_NAME)) {
                mongoTemplate.createCollection(COLLECTION_NAME);
                log.info("Created collection: " + COLLECTION_NAME);
            }
            if (!mongoTemplate.collectionExists(USAGE_COLLECTION_NAME)) {
                mongoTemplate.createCollection(USAGE_COLLECTION_NAME);
                log.info("Created collection: " + USAGE_COLLECTION_NAME);
            }
            if (!mongoTemplate.collectionExists(LOGINUSER_TABLE_NAME)) {
                mongoTemplate.createCollection(LOGINUSER_TABLE_NAME);
                log.info("Created collection: " + LOGINUSER_TABLE_NAME);
            }
        } catch (Exception e) {
            log.error("Failed to create collections: {}", e.getMessage());
        }
    }

    @Async
    public CompletableFuture<Integer> insertUsageData(String restCallId, String ipAddress, String timestamp) {
        int totalUsed = 0; // Initialize the variable to store total usage count

        try {
            // Try to find existing data
            Optional<UsageData> existingData = Optional.ofNullable(
                    mongoTemplate.findById(restCallId, UsageData.class, USAGE_COLLECTION_NAME));

            if (existingData.isPresent()) {
                // If data exists, update the usage count
                UsageData data = existingData.get();
                data.setTotalUsed(data.getTotalUsed() + 1); // Increment totalUsed
                data.setIpAddress(ipAddress);
                mongoTemplate.save(data, USAGE_COLLECTION_NAME);
                log.info("Updated totalUsed count to {} for restCallId {}", data.getTotalUsed(), restCallId);
                totalUsed = data.getTotalUsed(); // Store the updated totalUsed value
            } else {
                // If data does not exist, insert a new record with initial count
                UsageData newData = new UsageData(restCallId, ipAddress, 1, timestamp);
                mongoTemplate.save(newData, USAGE_COLLECTION_NAME);
                log.info("Inserted new usage entry for restCallId {}", restCallId);
                totalUsed = 1; // Initialize totalUsed as 1 for new entries
            }
        } catch (Exception e) {
            log.error("Failed to insert/update data in 'usage' collection: {}", e.getMessage());
        }

        // Return the totalUsed value asynchronously
        return CompletableFuture.completedFuture(totalUsed);
    }


    public void saveOrUpdateLink(String url, String data) {
        String id = generateSHA256Hash(url); // Unique ID based on URL

        // Check if the link already exists
        Optional<Link> existingLink = Optional.ofNullable(
                mongoTemplate.findById(id, Link.class, COLLECTION_NAME));

        if (existingLink.isPresent()) {
            // Update the existing record
            updateLink(id);
        } else {
            String author = llmService.callLLM(" who is the author of this article just provide the name and nothing else, if you cannot find the name just return unknown "+data);
            String keywords = llmService.callLLM(" what topics does this cover? give comma separated topics and limit it to top 5 ,for example java,programming,spring,awt,mongodb  "+data);

            // Insert new record
            createNewLink(id,url, author, data, keywords);
        }
    }



    @Override
    public void createNewLink(String id, String url, String author, String data, String keywords) {
        DocumentLink newLink = new DocumentLink(id,url,author,data,keywords, Instant.now(), Instant.now(), 1  );
        mongoTemplate.save(newLink, COLLECTION_NAME);
        log.info("New link added: " + url);
    }

    public void updateLink(String url) {
        // Logic to update existing link, for example, incrementing access count
        DocumentLink link = mongoTemplate.findById(url, DocumentLink.class, COLLECTION_NAME);
        if (link != null) {
            link.setTotalAccessCount(link.getTotalAccessCount() + 1);
            mongoTemplate.save(link, COLLECTION_NAME);
            log.info("Updated link: " + link.getUrl());
        }
    }

    @Override
    public List<Link> getTrendingArticlesInLastHour() {
        return List.of();
    }

    @Override
    public List<Link> getAllTimeTrendingArticles() {
        return List.of();
    }






}
