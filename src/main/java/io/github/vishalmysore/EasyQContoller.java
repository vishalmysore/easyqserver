package io.github.vishalmysore;

import io.github.vishalmysore.data.Link;
import io.github.vishalmysore.service.AWSDynamoService;
import io.github.vishalmysore.service.LLMService;
import io.github.vishalmysore.service.ScraperService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public String getQuestions(@RequestParam("prompt") String prompt) {
        log.info("received "+prompt);
     String jsonQustions = null;
     if(prompt.startsWith("https://") || prompt.startsWith("http://")) {

         String webData = scraperService.scrape(prompt);
          jsonQustions = llmService.buildQuestionsForLink(webData);
         if (jsonQustions.contains("```json") && jsonQustions.contains("```")) {
             int startIndex = jsonQustions.indexOf("```json") + 7; // Move past ```json
             int endIndex = jsonQustions.indexOf("```", startIndex); // Find closing ```

             if (endIndex != -1) {
                 jsonQustions = jsonQustions.substring(startIndex, endIndex).trim();

             }
         }
         dynamoService.saveOrUpdateLink(prompt,webData);
     } else {
         llmService.buildQuestionsForTopic(prompt);
         if (jsonQustions.contains("```json") && jsonQustions.contains("```")) {
             int startIndex = jsonQustions.indexOf("```json") + 7; // Move past ```json
             int endIndex = jsonQustions.indexOf("```", startIndex); // Find closing ```

             if (endIndex != -1) {
                 jsonQustions = jsonQustions.substring(startIndex, endIndex).trim();

             }
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
