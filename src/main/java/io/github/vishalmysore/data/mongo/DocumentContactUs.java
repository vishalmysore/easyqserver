package io.github.vishalmysore.data.mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "contactus")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentContactUs {
    private String type;
    private String gitUrl;
    private String email;
    private String message;
}
