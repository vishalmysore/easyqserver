package io.github.vishalmysore.data.mongo;

import io.github.vishalmysore.data.Question;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "articlescores")
public class ArticleScore {
    @Id
    private String id;
    private String linkid;
    private String userId;
    private int score;
    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private int skippedQuestions;
    private int totalScore;
    private int percentage;
    private String quizId;
    private String url;
    private String topics;
    private List<Question> questions;
    private long lastUpdatedTimestamp;
    private String quizType;
}
