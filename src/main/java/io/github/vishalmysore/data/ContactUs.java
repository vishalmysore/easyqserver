package io.github.vishalmysore.data;

import lombok.*;

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
}
