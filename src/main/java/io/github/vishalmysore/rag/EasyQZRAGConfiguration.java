package io.github.vishalmysore.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mongodb.atlas.MongoDBAtlasVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

@Configuration
public class EasyQZRAGConfiguration {




    private EmbeddingModel embeddingModel;

    private MongoTemplate mongoTemplate;
    public EasyQZRAGConfiguration(@Qualifier("openaimodel") EmbeddingModel embeddingModel,@Qualifier("mongoRagTemplate") MongoTemplate mongoTemplate) {

        this.embeddingModel =embeddingModel;
        this.mongoTemplate = mongoTemplate;
    }






    @Bean
    public VectorStore vectorStore() {
        return MongoDBAtlasVectorStore.builder(mongoTemplate,embeddingModel)
                .collectionName("easyqz_vector_store")           // Optional: defaults to "vector_store"
               // .vectorIndexName("custom_vector_index")          // Optional: defaults to "vector_index"
               // .pathName("custom_embedding")                    // Optional: defaults to "embedding"
                .numCandidates(500)                             // Optional: defaults to 200
                .metadataFieldsToFilter(List.of("userId", "date","type")) // Optional: defaults to empty list
                .initializeSchema(true)                         // Optional: defaults to false
                .batchingStrategy(new TokenCountBatchingStrategy()) // Optional: defaults to TokenCountBatchingStrategy
                .build();
    }

}
