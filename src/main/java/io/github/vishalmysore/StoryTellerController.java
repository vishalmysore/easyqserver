package io.github.vishalmysore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vishalmysore.data.NewsBlast;
import io.github.vishalmysore.data.QuizType;
import io.github.vishalmysore.data.Story;
import io.github.vishalmysore.service.LLMService;
import io.github.vishalmysore.service.NewsService;
import io.github.vishalmysore.service.base.StoryDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class StoryTellerController {
    @Autowired
    private LLMService llmService;
    @Autowired
    @Qualifier("storyDBService")
    private StoryDBService storyDBService;

    @Autowired
    private FileUtils fileUtils;
    @Value("${llmMode}")
    private String llmMode;

    @Autowired
    private NewsService newsService;
    @GetMapping("/getStory")
    public Story getStory(@RequestParam("category")  String category, @RequestParam("storyType")  String storyType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();  // Retrieve the userId set in the filter

        log.info("creating Story for " + userId);
        String prompt = "Can you give a story of type " + storyType + " with around 500 words? " +
                "It should not have any bad words or sensitive content. Also, please provide 10 questions based on the story. " +
                "The story and questions should be in JSON format: {title, storyText, question{  questionId" +
                "    questionText;\n" +
                "    List<String> answerChoices;\n" +
                "     String correctAnswer;\n" +
                "     String explanation;\n" +
                "     String userAnswer;}}.";

        if(category.equals("math")) {
            prompt = "I want to learn math, Can you give math concept for " + storyType + " with around 500 words? " +
                    "It should not have any bad words or sensitive content. Also, please provide 10 questions based on the concept. " +
                    "The concept and questions should be in JSON format: {title, storyText, question{  questionId" +
                    "    questionText;\n" +
                    "    List<String> answerChoices;\n" +
                    "     String correctAnswer;\n" +
                    "     String explanation;\n" +
                    "     String userAnswer;}}.";
        } else if (category.equals("science")) {
            prompt = "I want to learn science, Can you give a science concept of type " + storyType + " with around 500 words? " +
                    "It should not have any bad words or sensitive content. Also, please provide 10 questions based on the concept. " +
                    "The concept and questions should be in JSON format: {title, storyText, question{  questionId" +
                    "    questionText;\n" +
                    "    List<String> answerChoices;\n" +
                    "     String correctAnswer;\n" +
                    "     String explanation;\n" +
                    "     String userAnswer;}}.";
        }else  if(category.equals("history")) {
            prompt = "I need to learn history for exams, Can you give a history chapter of type " + storyType + " with around 500 words? " +
                    "It should not have any bad words or sensitive content. Also, please provide 10 questions based on the chapter. " +
                    "The chapter and questions should be in JSON format: {title, storyText, question{  questionId" +
                    "    questionText;\n" +
                    "    List<String> answerChoices;\n" +
                    "     String correctAnswer;\n" +
                    "     String explanation;\n" +
                    "     String userAnswer;}}.";
        } else if (category.equals("geography")) {
            prompt = "I need to write and exam for geography, Can you give a geography concept of type " + storyType + " with around 500 words? " +
                    "It should not have any bad words or sensitive content. Also, please provide 10 questions based on the concept. " +
                    "The concept and questions should be in JSON format: {title, storyText, question{  questionId" +
                    "    questionText;\n" +
                    "    List<String> answerChoices;\n" +
                    "     String correctAnswer;\n" +
                    "     String explanation;\n" +
                    "     String userAnswer;}}.";
        } else if (category.equals("civics")) {
            prompt = "I am learning civics for exams and you are my teacher, Can you give a civics chapter of type " + storyType + " with around 500 words? " +
                    "It should not have any bad words or sensitive content. Also, please provide 10 questions based on the chapter. " +
                    "The chapter and questions should be in JSON format: {title, storyText, question{  questionId" +
                    "    questionText;\n" +
                    "    List<String> answerChoices;\n" +
                    "     String correctAnswer;\n" +
                    "     String explanation;\n" +
                    "     String userAnswer;}}.";
        } else if (category.equals("news")) {
            prompt = "Can you give me current news for this subject " + storyType + " with around 500 words? " +
                    "It should not have any bad words or sensitive content. Also, please provide 10 questions based on the news. " +
                    "The news and questions should be in JSON format: {title, storyText, question{  questionId" +
                    "    questionText;\n" +
                    "    List<String> answerChoices;\n" +
                    "     String correctAnswer;\n" +
                    "     String explanation;\n" +
                    "     String userAnswer;}}.";
        }else if (category.equals("indoorsports")) {
            prompt = "You are my indoor sports teacher, Can you tell me a random concept about this indoor sport " + storyType + " with around 500 words? please include game rules as well" +
                    "It should not have any bad words or sensitive content. Also, please provide 10 questions based on the concept. " +
                    "The concept and questions should be in JSON format: {title, storyText, question{  questionId" +
                    "    questionText;\n" +
                    "    List<String> answerChoices;\n" +
                    "     String correctAnswer;\n" +
                    "     String explanation;\n" +
                    "     String userAnswer;}}.";
        } else if (category.equals("outdoorsports")) {
            prompt = "You are my coach for "+storyType+" Can you teach me about this outdoor sport " + storyType + " with around 500 words? include game rules, famous players, important match results etc " +
                    "It should not have any bad words or sensitive content. Also, please provide 10 questions based on the story. " +
                    "The concept and questions should be in JSON format: {title, storyText, question{  questionId" +
                    "    questionText;\n" +
                    "    List<String> answerChoices;\n" +
                    "     String correctAnswer;\n" +
                    "     String explanation;\n" +
                    "     String userAnswer;}}.";
        }
        String storyTextinJSON ="";
        if ("mock".equalsIgnoreCase(llmMode)) {

            storyTextinJSON = fileUtils.readFromResource("fakeStory.txt");


        } else if (llmMode == null || llmMode.isEmpty()) {
            // If llmMode is null or empty, get story from the LLM service
            storyTextinJSON = storyDBService.createStory(prompt,llmService);
        }
        String jsonStory = JsonUtils.fetchJson(storyTextinJSON);
        ObjectMapper objectMapper = new ObjectMapper();
        Story story = null;
        try {
           story= objectMapper.readValue(jsonStory, Story.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String storyId = QuizType.STORY.toString()+"_" +JsonUtils.generateUniqueIDForUser(userId);
        story.setStoryType(storyType);
        story.setUserId(userId);
        story.setStoryId(storyId);
        storyDBService.insertStory(story);
        return story;
    }


    @GetMapping("/getNews")
    public Story getNews(@RequestParam("category")  String category, @RequestParam("storyType")  String storyType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();  // Retrieve the userId set in the filter

        log.info("creating news for " + userId);
        NewsBlast newsBlast = newsService.getNews(storyType,10);
        String prompt = "You are a news reader, and this is the current news  " + newsBlast.getOverallNews() + " summarize with around 500 words " +
                "It should not have any bad words or sensitive content. Also, please provide 10 questions based on the news. " +
                "The news and questions should be in JSON format: {title, storyText, question{  questionId" +
                "    questionText;\n" +
                "    List<String> answerChoices;\n" +
                "     String correctAnswer;\n" +
                "     String explanation;\n" +
                "     String userAnswer;}}.";


        String storyTextinJSON ="";
        if ("mock".equalsIgnoreCase(llmMode)) {

            storyTextinJSON = fileUtils.readFromResource("fakeNews.txt");


        } else if (llmMode == null || llmMode.isEmpty()) {
            // If llmMode is null or empty, get story from the LLM service
            storyTextinJSON = storyDBService.createStory(prompt,llmService);
        }
        String jsonStory = JsonUtils.fetchJson(storyTextinJSON);
        ObjectMapper objectMapper = new ObjectMapper();
        Story story = null;
        try {
            story= objectMapper.readValue(jsonStory, Story.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String storyId = QuizType.STORY.toString()+"_" +JsonUtils.generateUniqueIDForUser(userId);
        story.setStoryType(storyType);
        story.setUserId(userId);
        story.setStoryId(storyId);
        story.setNewsBlast(newsBlast);
        storyDBService.insertStory(story);
        return story;
    }

}
