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
}
