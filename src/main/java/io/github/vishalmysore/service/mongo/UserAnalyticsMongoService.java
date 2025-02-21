package io.github.vishalmysore.service.mongo;

import io.github.vishalmysore.data.Score;
import io.github.vishalmysore.data.UserPerformance;
import io.github.vishalmysore.data.UserPerformanceData;
import io.github.vishalmysore.data.mongo.ArticleDetails;
import io.github.vishalmysore.data.mongo.ArticleScore;
import io.github.vishalmysore.service.base.UserAnalyticsDBService;
import io.github.vishalmysore.service.mongo.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
@Slf4j
@Service("UserAnalyticsDBService")
@ConditionalOnProperty(name = "easyQZ_DBTYPE", havingValue = "mongo", matchIfMissing = true)
public class UserAnalyticsMongoService implements UserAnalyticsDBService {
    private final ArticleScoreRepository articleScoreRepository;
    private final UserScoreRepository userScoreRepository;

    private final LoginUserRepository loginUserRepository;
    private final UserAnalyticsRepository userAnalyticsRepository;

    private final UserPerformanceDataRepository userPerformanceDataRepository;
    @Autowired
    public UserAnalyticsMongoService(ArticleScoreRepository articleScoreRepository, UserScoreRepository userScoreRepository, LoginUserRepository loginUserRepository,UserAnalyticsRepository userAnalyticsRepository,UserPerformanceDataRepository userPerformanceDataRepository) {
        this.articleScoreRepository = articleScoreRepository;
        this.userScoreRepository = userScoreRepository;
        this.loginUserRepository = loginUserRepository;
        this.userAnalyticsRepository = userAnalyticsRepository;
        this.userPerformanceDataRepository = userPerformanceDataRepository;
    }




    @Override
    public UserPerformance buildUserAnalytics(String userId) {
        // Fetch ArticleScores for the given user
        List<ArticleScore> articleScores = articleScoreRepository.findByUserId(userId);

        // Fetch the latest UserScore based on the timestamp
        Optional<Score> latestUserScoreOpt = userScoreRepository.findByUserIdOrderByTimestampDesc(userId).stream().findFirst();

        // Prepare the result object
        UserPerformance userPerformance = new UserPerformance();
        userPerformance.setUserId(userId);
        loginUserRepository.findByUserId(userId).ifPresent(loginUser -> {
            userPerformance.setEmailId(loginUser.getEmailId());
            userPerformance.setVerified(loginUser.isVerified());
            userPerformance.setAvatar(loginUser.getAvatar());
        });
        // Aggregate data for the user (latest UserScore)
        latestUserScoreOpt.ifPresent(userScore -> {
            userPerformance.setOverallScore(userScore.getOverallScore());
            userPerformance.setQuizType(userScore.getQuizType().toString());
        });

        // Track links frequency
        Map<String, Integer> linkFrequency = new HashMap<>();
        List<ArticleDetails> articleDetailsList = new ArrayList<>();

        // Tracking overall wrong answers and answers per topic
        int overallWrongAnswers = 0;
        Map<String, Integer> topicCorrectAnswers = new HashMap<>();
        Map<String, Integer> topicIncorrectAnswers = new HashMap<>();
        Map<String, Integer> topicAnsweredQuestions = new HashMap<>();

        for (ArticleScore score : articleScores) {
            ArticleDetails articleDetails = new ArticleDetails();

            // Deriving test name from topics
            String testName = String.join(", ", Arrays.asList(score.getTopics().split(",")));

            // Deriving date taken from lastUpdatedTimestamp
            Date dateTaken = new Date(score.getLastUpdatedTimestamp());

            // Getting main topics
            String mainTopics = score.getTopics();

            // Getting top links
            String link = score.getUrl();
            linkFrequency.put(link, linkFrequency.getOrDefault(link, 0) + 1);

            // Aggregate answers for strong and weak areas
            for (var question : score.getQuestions()) {
                String[] questionTopics = mainTopics.split(",");
                if (question.getCorrectAnswer().equals(question.getAnswerChoices())) {
                    for (String topic : questionTopics) {
                        topicCorrectAnswers.put(topic, topicCorrectAnswers.getOrDefault(topic, 0) + 1);
                    }
                } else {
                    overallWrongAnswers++;
                    for (String topic : questionTopics) {
                        topicIncorrectAnswers.put(topic, topicIncorrectAnswers.getOrDefault(topic, 0) + 1);
                    }
                }

                for (String topic : questionTopics) {
                    topicAnsweredQuestions.put(topic, topicAnsweredQuestions.getOrDefault(topic, 0) + 1);
                }
            }

            // Setting the details in the article object
            articleDetails.setTestName(testName);
            articleDetails.setScore(score.getScore());
            articleDetails.setDateTaken(dateTaken.toString());
            articleDetails.setMainTopics(mainTopics);
            articleDetails.setUrl(link);

            articleDetailsList.add(articleDetails);
        }

        // Getting the most frequent link (top link)
        String topLink = linkFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No top link");

        // Calculating strong and weak areas
        Map<String, String> strongAreas = new HashMap<>();
        Map<String, String> weakAreas = new HashMap<>();

        for (String topic : topicAnsweredQuestions.keySet()) {
            int answeredQuestions = topicAnsweredQuestions.get(topic);
            int correctAnswers = topicCorrectAnswers.getOrDefault(topic, 0);
            int incorrectAnswers = topicIncorrectAnswers.getOrDefault(topic, 0);

            double correctPercentage = (double) correctAnswers / answeredQuestions * 100;
            double incorrectPercentage = (double) incorrectAnswers / answeredQuestions * 100;

            if (correctPercentage > 70) {
                strongAreas.put(topic, "Strong");
            } else if (incorrectPercentage > 50) {
                weakAreas.put(topic, "Weak");
            }
        }

        // Set the overall wrong answers, strong/weak areas, and top link
        userPerformance.setArticles(articleDetailsList);
        userPerformance.setTopLink(topLink);
        userPerformance.setOverallWrongAnswers(overallWrongAnswers);
        userPerformance.setStrongAreas(strongAreas);
        userPerformance.setWeakAreas(weakAreas);
        userAnalyticsRepository.save(userPerformance);
        return userPerformance;
    }

    @Override
    public void updateUserAnalytics(UserPerformance userPerformance) {
        userAnalyticsRepository.save(userPerformance);
    }
    @Override
    public void updateUserPerformanceData(UserPerformanceData userPerformance) {
        userPerformanceDataRepository.save(userPerformance);
    }

    @Override
    public UserPerformance getUserAnalytics(String userId) {
        return userAnalyticsRepository.findByUserId(userId)
                .orElse(null);
    }
    @Override
    public UserPerformanceData getUserPerformanceData(String userId) {
        return userPerformanceDataRepository.findByUserId(userId)
                .orElse(null);
    }
}
