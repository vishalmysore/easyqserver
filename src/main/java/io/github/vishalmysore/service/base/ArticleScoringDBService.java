package io.github.vishalmysore.service.base;

import io.github.vishalmysore.chatter.EasyQNotificationHandler;
import io.github.vishalmysore.data.Link;
import io.github.vishalmysore.data.Score;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

public interface ArticleScoringDBService {
    public void insertScore(Score score, EasyQNotificationHandler easyQNotificationHandler);
    public  Link mapToLink(Map<String, AttributeValue> item);
    public Link getLinkByUrl(String url);

}
