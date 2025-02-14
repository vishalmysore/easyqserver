package io.github.vishalmysore.service;

import io.github.vishalmysore.service.base.*;
import io.github.vishalmysore.service.dynamo.*;
import io.github.vishalmysore.service.mongo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DBServiceConfig {

    @Value("${easyQZ_DBTYPE}")
    private String dbType;

    @Autowired
    private AWSDynamoService awsDynamoService;

    @Autowired
    private UserLoginDynamoService userLoginDynamoService;

    @Autowired
    private QuizResultsDynamoService quizResultsDynamoService;

    @Autowired
    private MongoService mongoService;

    @Autowired
    UserLoginMongoService userLoginMongoService;

    @Autowired
    QuizResultMongoService  quizResultMongoService;

    @Autowired
    ArticleScoringDynamoService articleScoringDynamoService;

    @Autowired
    ArticleScoringMongoService articleScoringMongoService;


    @Autowired
    private StoryStorageService storyStorageService;

    @Autowired
    private StoryMongoService storyMongoService;

    @Autowired
    private GoogleDynamoService googleDynamoService;

    @Autowired
    private GoogleMongoService googleMongoService;


    @Autowired
    private ContactUsDynamoService contactUsDynamoService;

    @Autowired
    private ContactUsMongoService contactUsMongoService;

    @Bean
    public BaseDBService dbService() {
        switch (dbType.toLowerCase()) {
            case "aws":
                return awsDynamoService;  // Return AWS service
            case "mongo":
                return mongoService;  // Return Mongo service
            default:
                throw new IllegalStateException("Unsupported DBTYPE: " + dbType);
        }
    }

    @Bean
    public UserLoginDBSrvice userLoginDBService() {
        if ("aws".equalsIgnoreCase(dbType)) {
            return userLoginDynamoService;
        } else if ("mongo".equalsIgnoreCase(dbType)) {
            return userLoginMongoService;
        }
        throw new IllegalStateException("Unsupported DBTYPE for UserLogin: " + dbType);
    }

    @Bean
    public QuizResultsDBService quizResultsDBService() {
        if ("aws".equalsIgnoreCase(dbType)) {
            return quizResultsDynamoService;
        } else if ("mongo".equalsIgnoreCase(dbType)) {
            return quizResultMongoService;
        }
        throw new IllegalStateException("Unsupported DBTYPE for QuizResults: " + dbType);
    }

    @Bean
    public ArticleScoringDBService articleScoringDBService() {
        if ("aws".equalsIgnoreCase(dbType)) {
            return articleScoringDynamoService;
        } else if ("mongo".equalsIgnoreCase(dbType)) {
            return articleScoringMongoService;
        }
        throw new IllegalStateException("Unsupported DBTYPE for ArticleScoringDBService: " + dbType);
    }

    @Bean
    public StoryDBService storyDBService() {
        if ("aws".equalsIgnoreCase(dbType)) {
            return storyStorageService;
        } else if ("mongo".equalsIgnoreCase(dbType)) {
            return storyMongoService;
        }
        throw new IllegalStateException("Unsupported DBTYPE for StoryDBService: " + dbType);
    }

    @Bean
    public GoogleDBService googleDBService() {
        if ("aws".equalsIgnoreCase(dbType)) {
            return googleDynamoService;
        } else if ("mongo".equalsIgnoreCase(dbType)) {
            return googleMongoService;
        }
        throw new IllegalStateException("Unsupported DBTYPE for GoogleDBService: " + dbType);
    }

    @Bean
    public ContactUsDBService contactUsDBService() {
        if ("aws".equalsIgnoreCase(dbType)) {
            return contactUsDynamoService;
        } else if ("mongo".equalsIgnoreCase(dbType)) {
            return contactUsMongoService;
        }
        throw new IllegalStateException("Unsupported DBTYPE for ContactUsDBService: " + dbType);
    }
}
