package io.github.vishalmysore.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.java.Log;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Log
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Document(collection = "loginuser")
@CompoundIndexes({
        @CompoundIndex(name = "userId_unique_idx", def = "{'userId' : 1}", unique = true)
})
public class NewUser {
    private String userId;
    private String userName;
    private String emailId;
    private String avatar;
}
