package io.github.vishalmysore.data;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "stories") // Maps to MongoDB Collectio
public class Story {
    private String storyId;
    private String storyText;
    private String userId;
    private String storyType;
    private String title;
    List<Question> questions;
    private String createdTimestamp;
}
