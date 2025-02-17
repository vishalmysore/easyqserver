package io.github.vishalmysore.data;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ArticleToReadAgain {
    private String link;
    private String specificReason;
}
