package io.github.vishalmysore.data.mongo;

import io.github.vishalmysore.data.Link;
import io.github.vishalmysore.data.Question;
import io.github.vishalmysore.data.QuizType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "articlescores")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentScore {

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
    private QuizType quizType;

    public Link toLink() {
        return new Link(
                url != null ? url : "unknown",
                "unknown", // Placeholder for author (modify if needed)
                0, // Placeholder for totalAccessCount (modify if needed)
                topics != null ? topics : "" // Using topics as keywords
        );
    }
}
