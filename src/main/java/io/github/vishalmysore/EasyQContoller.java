package io.github.vishalmysore;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vishalmysore.data.Link;
import io.github.vishalmysore.data.Question;
import io.github.vishalmysore.data.Quiz;
import io.github.vishalmysore.data.QuizType;
import io.github.vishalmysore.service.LLMService;
import io.github.vishalmysore.service.ScraperService;
import io.github.vishalmysore.service.base.BaseDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class EasyQContoller {

    @Autowired
    private LLMService llmService;

    @Autowired
    private ScraperService scraperService;

    @Autowired
    @Qualifier("dbService")
    private BaseDBService baseDBService;

    @Autowired
    private CacheManager cacheManager;
    @Value("${llmMode}")
    private String llmMode;

    @Autowired
    private FileUtils fileUtils;
    @GetMapping("/getQuestions")
    public Quiz getQuestions(@RequestParam("prompt") String prompt, @RequestParam("difficulty")  int difficulty) {
        log.info("received "+prompt);
        if(prompt.length() > 200) {
            throw new RuntimeException(" invalid prompt ");
        }
     String jsonQustions = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();  // Retrieve the userId set in the filter
        String quizId = JsonUtils.generateUniqueIDForUser(userId);
        if ("mock".equalsIgnoreCase(llmMode)) {

            jsonQustions = fileUtils.readFromResource("fakeQuestions.txt");


        } else if (llmMode == null || llmMode.isEmpty()) {

            if (prompt.startsWith("https://") || prompt.startsWith("http://")) {

                //String webData = getScrapedData(prompt);
                String webData = scraperService.scrape(prompt);
                jsonQustions = llmService.buildQuestionsForLink(webData, difficulty);
                if (jsonQustions.contains("```json") && jsonQustions.contains("```")) {
                    int startIndex = jsonQustions.indexOf("```json") + 7; // Move past ```json
                    int endIndex = jsonQustions.indexOf("```", startIndex); // Find closing ```

                    if (endIndex != -1) {
                        jsonQustions = jsonQustions.substring(startIndex, endIndex).trim();

                    }
                }
                baseDBService.saveOrUpdateLink(prompt, webData);
                quizId = QuizType.LINK.toString() + "_" + quizId;
            } else {
                quizId = QuizType.TOPIC.toString() + "_" + quizId;
                log.info("Prompt is not a link");
                jsonQustions = llmService.buildQuestionsForTopic(prompt, difficulty);
                if (jsonQustions.contains("```json") && jsonQustions.contains("```")) {
                    int startIndex = jsonQustions.indexOf("```json") + 7; // Move past ```json
                    int endIndex = jsonQustions.indexOf("```", startIndex); // Find closing ```

                    if (endIndex != -1) {
                        jsonQustions = jsonQustions.substring(startIndex, endIndex).trim();

                    }
                }
            }
        }
        List<Question> questions = new ArrayList<>();
        try {
            // Assuming jsonQuestions is now a JSON string containing an array of questions
            ObjectMapper objectMapper = new ObjectMapper();
            // Convert the string to a list of Question objects
            questions = objectMapper.readValue(jsonQustions, new TypeReference<List<Question>>(){});
        } catch (Exception e) {
            log.info("I got some issues let me try again on more time");
            jsonQustions = llmService.fixJson(jsonQustions);
            try {
                // Assuming jsonQuestions is now a JSON string containing an array of questions
                ObjectMapper objectMapper = new ObjectMapper();
                // Convert the string to a list of Question objects
                questions = objectMapper.readValue(jsonQustions, new TypeReference<List<Question>>(){});
            } catch (Exception e1) {
                log.error("LLM failed to generate questions");

            }

        }

        Quiz quiz = new Quiz();
        quiz.setQuizId(quizId);
        quiz.setQuestions(questions);

     return quiz;
    }


    @GetMapping("/getTrendingLastHour")
    public List<Link> getTrendingLastHour() {
        return baseDBService.getTrendingArticlesInLastHour();
    }

    @GetMapping("/getTrendingAll")
    public List<Link> getTrendingAll() {
        return baseDBService.getAllTimeTrendingArticles();
    }
}
