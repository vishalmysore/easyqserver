package io.github.vishalmysore.data;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Story {
    private String storyId;
    private String storyText;
    private String userId;
    private String storyType;
    private String title;
    List<Question> questions;
}
