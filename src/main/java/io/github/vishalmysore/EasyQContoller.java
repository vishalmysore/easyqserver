package io.github.vishalmysore;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vishalmysore.data.Link;
import io.github.vishalmysore.data.Question;
import io.github.vishalmysore.service.AWSDynamoService;
import io.github.vishalmysore.service.LLMService;
import io.github.vishalmysore.service.ScraperService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Log
@RestController
@RequestMapping("/api")
public class EasyQContoller {

    @Autowired
    private LLMService llmService;

    @Autowired
    private ScraperService scraperService;

    @Autowired
    AWSDynamoService dynamoService;

    @GetMapping("/getQuestions")
    public String getQuestions(@RequestParam("prompt") String prompt, int difficulty) {
        log.info("received "+prompt);
     String jsonQustions = null;
     if(prompt.startsWith("https://") || prompt.startsWith("http://")) {

         String webData = scraperService.scrape(prompt);
          jsonQustions = llmService.buildQuestionsForLink(webData,difficulty);
         if (jsonQustions.contains("```json") && jsonQustions.contains("```")) {
             int startIndex = jsonQustions.indexOf("```json") + 7; // Move past ```json
             int endIndex = jsonQustions.indexOf("```", startIndex); // Find closing ```

             if (endIndex != -1) {
                 jsonQustions = jsonQustions.substring(startIndex, endIndex).trim();

             }
         }
         dynamoService.saveOrUpdateLink(prompt,webData);
     } else {
         log.info("Prompt is not a link");
         jsonQustions =  llmService.buildQuestionsForTopic(prompt,difficulty);
         if (jsonQustions.contains("```json") && jsonQustions.contains("```")) {
             int startIndex = jsonQustions.indexOf("```json") + 7; // Move past ```json
             int endIndex = jsonQustions.indexOf("```", startIndex); // Find closing ```

             if (endIndex != -1) {
                 jsonQustions = jsonQustions.substring(startIndex, endIndex).trim();

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
                log.severe("LLM failed to generate questions");

            }

        }
     return jsonQustions;
    }

    @GetMapping("/getTrendingLastHour")
    public List<Link> getTrendingLastHour() {
        return dynamoService.getTrendingArticlesInLastHour();
    }

    @GetMapping("/getTrendingAll")
    public List<Link> getTrendingAll() {
        return dynamoService.getAllTimeTrendingArticles();
    }
}
