package io.github.vishalmysore.service.mongo;

import io.github.vishalmysore.chatter.EasyQScoreHandler;
import io.github.vishalmysore.data.QuizType;
import io.github.vishalmysore.data.Score;
import io.github.vishalmysore.data.mongo.DocumentLink;
import io.github.vishalmysore.service.base.QuizResultsDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service("quizResultsDBService")
@ConditionalOnProperty(name = "easyQZ_DBTYPE", havingValue = "mongo", matchIfMissing = true)
public class QuizResultMongoService extends  MongoService implements QuizResultsDBService {
    @Async
    public void insertScore(Score score, EasyQScoreHandler easyQScoreHandler) {
        try {
            if (mongoTemplate == null) {
                log.error("MongoTemplate is not initialized. Ensure proper configuration.");
                return;
            }

            String id = "Story_Topic";

            if (score.getQuizType().equals(QuizType.LINK)) {
                id = generateSHA256Hash(score.getUrl());

                // Fetch link details from the "links" collection in MongoDB
                Query query = new Query(Criteria.where("id").is(id));
                DocumentLink linkData = mongoTemplate.findOne(query, DocumentLink.class, "links");

                // Set topic details from the fetched link data
                String topics = (linkData != null) ? linkData.getKeywords() : "No topics found";

                score.setTopics(topics);
                score.setLinkId(id);
                score.setUrl(score.getUrl());

            } else if (score.getQuizType().equals(QuizType.TOPIC)) {
                score.setTopics(score.getTopics());
                score.setLinkId("NA");
                score.setUrl("NA");

            } else if (score.getQuizType().equals(QuizType.STORY)) {
                score.setTopics("story");
                score.setLinkId("NA");
            }

            // Fetch the latest score record for the user
            Query userQuery = new Query(Criteria.where("userId").is(score.getUserId()))
                    .limit(1)
                    .with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "timestamp"));

            Score latestScore = mongoTemplate.findOne(userQuery, Score.class, USER_LATEST_SCORE);
            int overallScore = (latestScore != null) ? latestScore.getOverallScore() + score.getTotalScore() : score.getTotalScore();

            score.setOverallScore(overallScore);
            score.setTimestamp(Instant.now().toEpochMilli());

            // Insert the new score record
            mongoTemplate.insert(score, USER_LATEST_SCORE);
            log.info("New score inserted successfully for userId: {}, quizId: {}, with linkId: {} in collection: {}",
                    score.getUserId(), score.getQuizId(), id, USER_LATEST_SCORE);

            easyQScoreHandler.sendScoreToUser(score.getUserId(), (double) overallScore);
        } catch (Exception e) {
            log.error("Error occurred while inserting new score: {}", e.getMessage());
        }
    }

    @Override
    public Double getOverallScore(String userId) {
        try {
            // Prepare the query to fetch the latest record for the user
            Query query = new Query(Criteria.where("userId").is(userId))
                    .with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "timestamp"))
                    .limit(1);  // Fetch only the latest record

            // Execute the query to get the latest score record
            List<Score> scores = mongoTemplate.find(query, Score.class, USER_LATEST_SCORE);

            if (scores.isEmpty()) {
                log.warn("No records found for user: " + userId);
                return 0.0;  // No records found, return 0
            }

            // Get the latest record
            Score latestScore = scores.get(0);

            // Return the overall score
            Double overallScore = (double) latestScore.getOverallScore();
            log.info("Latest overall score for user " + userId + ": " + overallScore);
            return overallScore;

        } catch (Exception e) {
            log.error("Error retrieving latest overall score for user " + userId + ": " + e.getMessage());
            return 0.0;  // Return default 0.0 in case of error
        }
    }
}
