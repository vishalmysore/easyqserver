package io.github.vishalmysore.data.mongo;

import io.github.vishalmysore.data.Link;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "links")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentLink {
    @Id
    private String id;
    private String url;
    private String author;
    private String data;
    private String keywords;
    private Instant firstUsed;
    private Instant lastUsed;
    private int totalAccessCount;

    public Link toLink() {
        return new Link(url, author, totalAccessCount, keywords);
    }
    // Getters and Setters
}
