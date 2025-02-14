package io.github.vishalmysore.service.mongo;

import io.github.vishalmysore.chatter.EasyQScoreHandler;
import io.github.vishalmysore.data.Score;
import io.github.vishalmysore.service.base.QuizResultsDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("quizResultMongoService")
public class QuizResultMongoService extends  MongoService implements QuizResultsDBService {
    @Override
    public void insertScore(Score score, EasyQScoreHandler easyQScoreHandler) {
        log.info("Inserting score: " + score );
    }

    @Override
    public Double getOverallScore(String userId) {
        log.info("Getting overall score for userId: " + userId);
        return 0.0;
    }
}
