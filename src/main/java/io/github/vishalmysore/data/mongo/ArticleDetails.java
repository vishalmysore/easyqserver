package io.github.vishalmysore.data.mongo;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDetails {
    private String testName;
    private int score;
    private String dateTaken;
    private String mainTopics;
    private String url;

    // Getters and Setters
}
