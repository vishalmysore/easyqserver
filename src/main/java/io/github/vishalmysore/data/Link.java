package io.github.vishalmysore.data;

import lombok.*;
import lombok.extern.java.Log;

@Log
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Link {
    private String url;
    private String author;
    private int totalAccessCount;
}
