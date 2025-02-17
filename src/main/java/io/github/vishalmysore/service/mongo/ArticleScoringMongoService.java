package io.github.vishalmysore.service.mongo;

import io.github.vishalmysore.chatter.EasyQNotificationHandler;
import io.github.vishalmysore.data.Link;
import io.github.vishalmysore.data.Score;
import io.github.vishalmysore.data.mongo.DocumentScore;
import io.github.vishalmysore.service.base.ArticleScoringDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service ("articleScoringDBService")
@ConditionalOnProperty(name = "easyQZ_DBTYPE", havingValue = "mongo", matchIfMissing = true)
public class ArticleScoringMongoService extends  MongoService implements ArticleScoringDBService {
    @Override
    @Async
    public void insertScore(Score score, EasyQNotificationHandler easyQNotificationHandler) {
        try {

            // Generate a unique linkId based on the URL or other identifier
            String id = generateSHA256Hash(score.getUrl());

            // Create a new DocumentScore to insert into MongoDB
            DocumentScore documentScore = new DocumentScore();
            documentScore.setLinkid(id);  // Setting the linkId (mapped to 'id' in Mongo)
            documentScore.setUserId(score.getUserId());
            documentScore.setScore(score.getScore());
            documentScore.setTotalQuestions(score.getTotalQuestions());
            documentScore.setCorrectAnswers(score.getCorrectAnswers());
            documentScore.setIncorrectAnswers(score.getIncorrectAnswers());
            documentScore.setSkippedQuestions(score.getSkippedQuestions());
            documentScore.setTotalScore(score.getTotalScore());
            documentScore.setPercentage(score.getPercentage());
            documentScore.setQuizId(score.getQuizId());
            documentScore.setUrl(score.getUrl());
            documentScore.setTopics(score.getTopics());
            documentScore.setQuestions(score.getQuestions());  // Directly mapping the list of Question objects

            // Assuming the QuizType is a part of the Score, you can also set it directly
            documentScore.setQuizType(score.getQuizType());

            // Set the current timestamp for the lastUpdated field
            documentScore.setLastUpdatedTimestamp(System.currentTimeMillis());

            // Insert the new DocumentScore into MongoDB
            mongoTemplate.save(documentScore);

            log.info("New score inserted successfully for userId: " + score.getUserId() + " and quizId: " + score.getQuizId() + " with score: " + score.getScore());

        } catch (Exception e) {
            log.error("Error occurred while inserting new score: " + e.getMessage());
        }
    }



    public Link getLinkByUrl(String url) {
        // Generate the unique id (SHA-256 hash of the URL)
        String id = generateSHA256Hash(url);
        log.info("Fetching link with linkid: {}", id);

        try {
            // Query MongoDB collection for the document using the generated id
            Query query = new Query(Criteria.where("linkid").is(id));

            // Find the link by id
            DocumentScore link = mongoTemplate.findOne(query, DocumentScore.class);

            if (link == null) {
                throw new RuntimeException("Link not found with id: " + id);
            }

            return link.toLink();
        } catch (Exception e) {
            log.error("Error occurred while fetching link by URL: " + e.getMessage());
            throw new RuntimeException("Error occurred while fetching link by URL: " + e.getMessage());
        }
    }



}
