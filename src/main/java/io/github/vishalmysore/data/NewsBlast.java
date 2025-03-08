package io.github.vishalmysore.data;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Setter
@Slf4j
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NewsBlast {
    private String overallNews;
    private List<String> titles;
    private List<String> links;
    private List<String> descriptions;
}
