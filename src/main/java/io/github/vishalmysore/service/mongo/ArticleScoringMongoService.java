package io.github.vishalmysore.service.mongo;

import io.github.vishalmysore.chatter.EasyQNotificationHandler;
import io.github.vishalmysore.data.Link;
import io.github.vishalmysore.data.Score;
import io.github.vishalmysore.service.base.ArticleScoringDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

@Slf4j
@Service
public class ArticleScoringMongoService extends  MongoService implements ArticleScoringDBService {
    @Override
    public void insertScore(Score score, EasyQNotificationHandler easyQNotificationHandler) {
        log.info("Inserting score: " + score );
    }

    @Override
    public Link mapToLink(Map<String, AttributeValue> item) {
        return null;
    }

    @Override
    public Link getLinkByUrl(String url) {
        return null;
    }


}
