package io.github.vishalmysore.service.base;

import io.github.vishalmysore.chatter.EasyQScoreHandler;
import io.github.vishalmysore.data.Score;

public interface QuizResultsDBService {

    public void insertScore(Score score, EasyQScoreHandler easyQScoreHandler);
    public Double getOverallScore(String userId);
}
