package io.github.vishalmysore;

import io.github.vishalmysore.chatter.EasyQNotificationHandler;
import io.github.vishalmysore.chatter.EasyQScoreHandler;
import io.github.vishalmysore.data.Link;
import io.github.vishalmysore.data.QuizType;
import io.github.vishalmysore.data.Score;
import io.github.vishalmysore.service.base.ArticleScoringDBService;
import io.github.vishalmysore.service.base.QuizResultsDBService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Log
public class ResultsContoller {


    private QuizResultsDBService quizResultsDBService;

    private ArticleScoringDBService articleScoringDBService;


    private EasyQScoreHandler easyQScoreHandler;

    private EasyQNotificationHandler easyQNotificationHandler;

    private NotificationController notificationController;

    @Autowired
    public ResultsContoller(@Qualifier("quizResultsDBService") QuizResultsDBService quizResultsDBService,
                            @Qualifier("articleScoringDBService") ArticleScoringDBService articleScoringDBService,
                     EasyQScoreHandler easyQScoreHandler, EasyQNotificationHandler easyQNotificationHandler, NotificationController notificationController) {
        this.quizResultsDBService = quizResultsDBService;
        this.articleScoringDBService = articleScoringDBService;
        this.easyQScoreHandler = easyQScoreHandler;
        this.easyQNotificationHandler = easyQNotificationHandler;
        this.notificationController = notificationController;
    }



    @PostMapping("/updateResults")
    public Score updateResults(@RequestBody Score score, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();  // Retrieve the userId set in the filter
        score.setUserId(userId);
        log.info("Getting results "+score);
        quizResultsDBService.insertScore(score,easyQScoreHandler);
        articleScoringDBService.insertScore(score,easyQNotificationHandler);
        if(score.getQuizType().equals(QuizType.LINK)) {
            Link link = articleScoringDBService.getLinkByUrl(score.getUrl());
            score.setTopics(link.getKeywords());
        } else {
            score.setUrl(score.getTopics());
        }

        notificationController.sendNotification("newTestTaken", score);

        return score;
    }

    @GetMapping("/getOverAllScore")
    public Double getOverAllScore(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();  // Retrieve the userId set in the filter
        log.info("Getting overall score for "+userId);
        return quizResultsDBService.getOverallScore(userId);
    }


}
