package io.github.vishalmysore.service.mongo;

import io.github.vishalmysore.data.Story;
import io.github.vishalmysore.service.base.StoryDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service("storyDBService")
@Slf4j
@ConditionalOnProperty(name = "easyQZ_DBTYPE", havingValue = "mongo", matchIfMissing = true)
public class StoryMongoService extends MongoService implements StoryDBService {
    @Async
    public void insertStory(Story story) {
        try {
            if (mongoTemplate == null) {
                log.error("MongoTemplate is not initialized. Ensure that it's properly configured.");
                return;
            }

            // Set the created timestamp
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTimestamp = LocalDateTime.now().format(formatter);

            // Assign timestamp to story object
            story.setCreatedTimestamp(formattedTimestamp);

            // Insert story into MongoDB
            mongoTemplate.insert(story, STORIES_TABLE_NAME);
            log.info("New story inserted successfully for userId: {} and storyId: {}", story.getUserId(), story.getStoryId());

           // return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error occurred while inserting new story: {}", e.getMessage());
            //return CompletableFuture.failedFuture(e);
        }
    }

}
