package io.github.vishalmysore.data.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "loginuser")
public class LoginUser {
    @Id
    private String id;
    private Instant createdTimestamp;
    private boolean verified;
    private String ipAddress;
    private String emailId;
    private String userId;
    private boolean tempUser;
    private Instant lastLoggedInTimestamp;
    private String avatar;
}
