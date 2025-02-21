package io.github.vishalmysore.rag;

import com.mongodb.client.MongoClients;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class EmbeddingConf {
    private final String ragDBURI ;

    private final String ragDBName ;
    private EmbeddingModel embeddingModel;

    private MongoTemplate mongoTemplate;

    @Value("${rag.openai.key}")
    private String ragOpenAIAPIKey;
    public EmbeddingConf(@Value("${rag.mongo.uri}") String ragDBURI, @Value("${rag.mongo.dbname}") String ragDBName) {
        this.ragDBName =ragDBName;
        this.ragDBURI =ragDBURI;

    }
    @Bean (name="openaimodel")
    public EmbeddingModel embeddingModel() {
        this.embeddingModel = new OpenAiEmbeddingModel(new OpenAiApi(ragOpenAIAPIKey));
        return embeddingModel;
    }

    @Bean(name = "mongoRagTemplate")
    public MongoTemplate mongoRagTemplate() {
        this.mongoTemplate = new MongoTemplate(MongoClients.create(ragDBURI), ragDBName);
        return mongoTemplate;
    }

}
