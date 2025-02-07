package io.github.vishalmysore.data;

import lombok.*;
import lombok.extern.java.Log;

import java.util.List;

@Log
@ToString
@EqualsAndHashCode
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Score {
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

    private QuizType quizType;

    public void setQuizId(String quizId) {
        this.quizId = quizId;

        // Set quizType based on the quizId prefix
        if (quizId != null) {
            // Iterate through all QuizType values
            for (QuizType type : QuizType.values()) {
                if (quizId.toLowerCase().startsWith(type.toString().toLowerCase())) {
                    this.quizType = type;
                    return;  // Exit the loop once a match is found
                }
            }

            // If no match is found, default to TOPIC (or another default type)
            this.quizType = QuizType.TOPIC;
        }
    }
}
