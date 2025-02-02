package io.github.vishalmysore.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.java.Log;

@Log
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class NewUser {
    private String userId;
    private String userName;
    private String emailId;
}
