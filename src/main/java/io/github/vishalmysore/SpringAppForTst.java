package io.github.vishalmysore;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vishalmysore.data.Story;
import io.github.vishalmysore.service.LLMService;

public class SpringAppForTst {
    public static void main(String[] args) throws JsonProcessingException {
        LLMService llmService = new LLMService("modelName"); // Manually create an instance
        String storyType = "horror"; // Example: Change this dynamically
        String prompt = "Can you give a story of type " + storyType + " with around 500 words? " +
                "It should not have any bad words or sensitive content. Also, please provide 10 questions based on the story. " +
                "The story and questions should be in JSON format: {title, storyText, question{  questionId" +
                "    questionText;\n" +
                "    List<String> answerChoices;\n" +
                "     String correctAnswer;\n" +
                "     String explanation;\n" +
                "     String userAnswer;}}.";

        String storyTextInJSON = llmService.callLLM(prompt);
        System.out.println("Generated Story: " + storyTextInJSON);
        ObjectMapper objectMapper = new ObjectMapper();
        storyTextInJSON =JsonUtils.fetchJson(storyTextInJSON);
       Story story = objectMapper.readValue(storyTextInJSON, Story.class); // Validate JSON
        System.out.println(story);
    }
}
