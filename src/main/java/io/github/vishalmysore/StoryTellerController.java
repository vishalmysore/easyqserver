package io.github.vishalmysore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vishalmysore.data.Story;
import io.github.vishalmysore.service.LLMService;
import io.github.vishalmysore.service.StoryStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private StoryStorageService storyStorageService;
    @GetMapping("/getStory")
    public Story getStory(@RequestParam("userId") String userId, @RequestParam("storyType")  String storyType) {
        log.info("creating Story for " + userId);
        String prompt = "Can you give a story of type " + storyType + " with around 500 words? " +
                "It should not have any bad words or sensitive content. Also, please provide 10 questions based on the story. " +
                "The story and questions should be in JSON format: {title, storyText, question{  questionId" +
                "    questionText;\n" +
                "    List<String> answerChoices;\n" +
                "     String correctAnswer;\n" +
                "     String explanation;\n" +
                "     String userAnswer;}}.";
        String storyTextinJSON = llmService.callLLM(prompt);
        String jsonStory = JsonUtils.fetchJson(storyTextinJSON);
        ObjectMapper objectMapper = new ObjectMapper();
        Story story = null;
        try {
           story= objectMapper.readValue(jsonStory, Story.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String storyId = System.currentTimeMillis() + userId;
        story.setStoryType(storyType);
        story.setUserId(userId);
        story.setStoryId(storyId);
        storyStorageService.insertStory(story);
        return story;
    }


}
