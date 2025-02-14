package io.github.vishalmysore.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUser {
    private String sub;  // User ID
    private String email;
    private String name;
    private String createdTimestamp;  // Added timestamp for creation
    private String easyQZUserId;
}
