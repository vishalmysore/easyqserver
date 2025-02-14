package io.github.vishalmysore.service.mongo;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "easyQZ_DBTYPE", havingValue = "mongo", matchIfMissing = true)
public class MongoConfig {

    @Value("${spring.data.mongodb.host}")
    private String mongoHost;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Value("${spring.data.mongodb.username}")
    private String username;

    @Value("${spring.data.mongodb.password}")
    private String password;

    @Value("${spring.data.mongodb.params}")
    private String connectionParams;

    @Value("${easyQZ_DBTYPE}")
    private String dbType;

    public MongoConfig() {
        log.info("MongoConfig constructor called");
    }

    public String buildMongoUri() {
        StringBuilder uriBuilder = new StringBuilder();

        // Construct Mongo URI for username, password and connection
        uriBuilder.append("mongodb+srv://")
                .append(username)
                .append(":")
                .append(password)
                .append("@")
                .append(mongoHost);

        // If there are additional connection parameters, append them
        if (connectionParams != null && !connectionParams.isEmpty()) {
            uriBuilder.append("/")
                    .append(databaseName)
                    .append("?")
                    .append(connectionParams);
        } else {
            uriBuilder.append("/")
                    .append(databaseName);
        }

        return uriBuilder.toString();
    }

    // MongoDB configuration only enabled when DBTYPE is set to MongoDB
    @Bean
    public MongoClient mongoClient() {
        // Use the connection string for MongoDB Atlas
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new com.mongodb.ConnectionString(buildMongoUri())) // Use the URI built with all credentials and parameters
                .applyToConnectionPoolSettings(builder ->
                        builder.maxWaitTime(5000, TimeUnit.MILLISECONDS) // Example for connection pooling
                )
                .build();

        return MongoClients.create(settings);
    }
}