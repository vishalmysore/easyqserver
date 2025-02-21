package io.github.vishalmysore.data;

import io.github.vishalmysore.data.mongo.ArticleDetails;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_performance_data")
public class UserPerformanceData {


    private List<String> topSkills;
    private List<String> bottomSkills;
    private int overallScore;
    private List<String> improvements;
    private List<ArticleToReadAgain> articlesToReadAgain;
    private String textSummary;
    @Id
    private String userId;
    private String emailId;
    private String avatar;
    boolean verified;
    private Instant createdTime;
    private List<ArticleDetails> articles;

}
