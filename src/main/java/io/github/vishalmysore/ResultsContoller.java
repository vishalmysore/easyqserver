package io.github.vishalmysore;

import io.github.vishalmysore.data.Score;
import io.github.vishalmysore.service.ArticleScoringDBService;
import io.github.vishalmysore.service.QuizResultsDynamoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Log
public class ResultsContoller {

    @Autowired
    private QuizResultsDynamoService quizResultsDynamoService;
    @Autowired
    private ArticleScoringDBService articleScoringDBService;

    @PostMapping("/updateResults")
    public String updateResults(@RequestBody Score score, HttpServletRequest request) {
        log.info("Getting results "+score);
        quizResultsDynamoService.insertScore(score);
        articleScoringDBService.insertScore(score);
        return "Results";
    }

}
