package io.github.vishalmysore.data;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Quiz {
    private String quizId;
    private List<Question> questions;
}
