package io.github.vishalmysore.data;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ContactUs {
    private String type;
    private String gitUrl;
    private String email;
    private String message;
    @CreatedDate
    private Instant createdTime;
}
