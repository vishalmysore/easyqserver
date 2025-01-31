package io.github.vishalmysore.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.java.Log;

@Log
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Link {
    private String url;
    private String author;
    private int totalAccessCount;
}
