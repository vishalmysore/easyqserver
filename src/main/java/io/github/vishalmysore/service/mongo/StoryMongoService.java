package io.github.vishalmysore.service.mongo;

import io.github.vishalmysore.data.Story;
import io.github.vishalmysore.service.base.StoryDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StoryMongoService extends MongoService implements StoryDBService {
    @Override
    public void insertStory(Story story) {
        log.info("Inserting story: " + story);
    }


}
