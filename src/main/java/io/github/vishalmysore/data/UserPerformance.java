package io.github.vishalmysore.data;

import io.github.vishalmysore.data.mongo.ArticleDetails;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_performance")
public class UserPerformance {
    @Id
    private String userId;
    private String avatar;
    private String emailId;
    private boolean verified;
    private int overallScore;
    private String quizType;
    private List<ArticleDetails> articles;
    private String topLink;
    private int overallWrongAnswers;
    private Map<String, String> strongAreas;
    private Map<String, String> weakAreas;

    // Getters and Setters
}
