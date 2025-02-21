package io.github.vishalmysore.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RAGService {

    @Autowired
    private VectorStore vectorStore;

    public void addResult(String data) {
        vectorStore.add(List.of(new Document(data)));
    }

    public void purgeData(List<String> userIds) {

        Filter.Expression filterExpression = new Filter.Expression(
                Filter.ExpressionType.IN,  // Using IN for matching multiple userIds
                new Filter.Key("userId"),  // Field to filter on
                new Filter.Value(userIds)  // List of userIds to match
        );




        //String filterExpression = "userId IN (" + String.join(", ", userIds) + ")";

        // Now use the filter expression in the delete call
        vectorStore.delete( filterExpression);
    }

    public void addResult(String data, Map<String,Object> map) {
        vectorStore.add(List.of(new Document(data, map)));

    }
    public List<Document> getResult(String query) {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(1).build());
        //you can do additional processing here if needed
        return results;
    }

    public List<Document> getResult(String query,String userId) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(1).filterExpression((b.in("userId",userId)).build()).build());
        //you can do additional processing here if needed
        return results;
    }
}
