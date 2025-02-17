package io.github.vishalmysore.data.mongo;

import io.github.vishalmysore.data.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "story")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentStory {
    @Id
    private String storyId;
    private String storyText;
    private String userId;
    private String storyType;
    private String title;
    List<Question> questions;
}
