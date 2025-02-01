package io.github.vishalmysore.data;


import lombok.*;
import lombok.extern.java.Log;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Log
@ToString
public class Question {
    private String questionId;
    private String questionText;
    private List<String> answerChoices;
    private String correctAnswer;
}
