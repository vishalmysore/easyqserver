package io.github.vishalmysore.data;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UsageData {
    private String restCallId;
    private String ipAddress;
    private int totalUsed;
    private String timestamp;

}
