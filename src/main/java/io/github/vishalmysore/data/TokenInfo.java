package io.github.vishalmysore.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo {
    private String aud;  // Audience (your client ID)
    private String email;
    private String sub;  // Google user ID

    // Getters and setters
}
