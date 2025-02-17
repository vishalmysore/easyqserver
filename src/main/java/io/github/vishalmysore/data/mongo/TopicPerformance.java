package io.github.vishalmysore.data.mongo;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TopicPerformance {
    private String topic;
    private int correctAnswers;
    private int incorrectAnswers;
    private String areaStrength;

    // Getters and Setters
}
