package io.github.vishalmysore.service.base;

import io.github.vishalmysore.chatter.EasyQNotificationHandler;
import io.github.vishalmysore.data.Link;
import io.github.vishalmysore.data.Score;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

public interface ArticleScoringDBService {
    public void insertScore(Score score, EasyQNotificationHandler easyQNotificationHandler);

    public Link getLinkByUrl(String url);
    public default Link mapToLink(Map<String, AttributeValue> item) {
        return new Link(
                item.get("url").s(),
                item.get("author").s(),
                Integer.parseInt(item.get("totalAccessCount").n()),
                item.get("keywords").s()
        );
    }
}
