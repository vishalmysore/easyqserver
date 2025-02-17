package io.github.vishalmysore.service.mongo;

import io.github.vishalmysore.data.Link;
import io.github.vishalmysore.data.mongo.DocumentLink;
import io.github.vishalmysore.service.LLMService;
import io.github.vishalmysore.service.base.BaseDBService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service("dbService")
public class MongoService implements BaseDBService {

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    private LLMService llmService;
    @Value("${easyQZ_DBTYPE}")
    private String dbType;

    private static final String COLLECTION_NAME = "links";
    protected static final String USAGE_COLLECTION_NAME = "usage"; // Holds usage data like rest calls and IP addresses
    protected  String LOGINUSER_TABLE_NAME ="loginuser";

    protected  final String STORIES_TABLE_NAME = "stories";

    protected static final String USER_LATEST_SCORE = "user_score";

    protected static final String GOOGLEUSERS_TABLE_NAME = "google_users";

    protected static final String CONTACTUS_TABLE_NAME = "contactus";

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
                mongoTemplate.indexOps(USAGE_COLLECTION_NAME).ensureIndex(
                        new Index().on("timestamp", org.springframework.data.domain.Sort.Direction.ASC)
                                .expire(259200) // 3 days (in seconds)
                );

                log.info("TTL index set on timestamp field: 3 days expiry");
            }
            if (!mongoTemplate.collectionExists(LOGINUSER_TABLE_NAME)) {
                mongoTemplate.createCollection(LOGINUSER_TABLE_NAME);
                log.info("Created collection: " + LOGINUSER_TABLE_NAME);
            }
            if (!mongoTemplate.collectionExists(STORIES_TABLE_NAME)) {
                mongoTemplate.createCollection(STORIES_TABLE_NAME);
                log.info("Created collection: " + STORIES_TABLE_NAME);
            }
            if (!mongoTemplate.collectionExists(USER_LATEST_SCORE)) {
                mongoTemplate.createCollection(USER_LATEST_SCORE);
                log.info("Created collection: " + USER_LATEST_SCORE);
            }

            if (!mongoTemplate.collectionExists(GOOGLEUSERS_TABLE_NAME)) {
                mongoTemplate.createCollection(GOOGLEUSERS_TABLE_NAME);
                log.info("Created collection: " + GOOGLEUSERS_TABLE_NAME);
            }

            if (!mongoTemplate.collectionExists(CONTACTUS_TABLE_NAME)) {
                mongoTemplate.createCollection(CONTACTUS_TABLE_NAME);
                log.info("Created collection: " + CONTACTUS_TABLE_NAME);
                mongoTemplate.indexOps(CONTACTUS_TABLE_NAME).ensureIndex(
                        new Index().on("createdTime", org.springframework.data.domain.Sort.Direction.ASC)
                                .expire(259200) // 3 days (in seconds)
                );

                log.info("TTL index set on timestamp field: 3 days expiry");
            }
        } catch (Exception e) {
            log.error("Failed to create collections: {}", e.getMessage());
        }
    }

   /* @Async
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
    */



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
        long oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000); // Current time minus 1 hour
        Query query = new Query();
        query.addCriteria(Criteria.where("lastUsed").gte(new Date(oneHourAgo))); // Articles accessed in the last hour
        query.with(Sort.by(Sort.Order.desc("totalAccessCount"))); // Sort by total access count (most accessed first)
        List<DocumentLink> documentLinks = mongoTemplate.find(query, DocumentLink.class);
        return documentLinks.stream()
                .map(DocumentLink::toLink)  // Convert each DocumentLink to Link
                .collect(Collectors.toList());
    }

    @Override
    public List<Link> getAllTimeTrendingArticles() {
        Query query = new Query();
        query.with(Sort.by(Sort.Order.desc("totalAccessCount"))); // Sort by total access count

        List<DocumentLink> documentLinks = mongoTemplate.find(query, DocumentLink.class);

        // Convert DocumentLink to Link using toLink() method and return as List<Link>
        return documentLinks.stream()
                .map(DocumentLink::toLink)  // Convert each DocumentLink to Link
                .collect(Collectors.toList());  // Collect the results into a List<Link>
    }





}
