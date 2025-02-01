package io.github.vishalmysore.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

@Log
@Service
public class LLMService {

    private final ChatLanguageModel chatModel;

    // Constructor to initialize the model once
    public LLMService() {

        this.chatModel = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName("gpt-4o-mini-2024-07-18")  // Make sure you provide the correct model name
                .build();
    }

    public String callLLM(String prompt){

        String result = chatModel.chat(prompt);

        log.info("Prompt: " + prompt);
        log.info("Result: " + result);

        return result;
    }



    public String buildQuestionsForLink(String data, int difficulty){
        String prompt = "Can you build 10 multiple choice questions from the following data - \n{{" + data + " }}\n in Json format, the json should contain only these fields- questionId,questionText,answerChoices,correctAnswer\n";
        prompt = enhancePrompt(difficulty, prompt);
        return callLLM(prompt);
    }

    private static String enhancePrompt(int difficulty, String prompt) {
        if(difficulty == 2) {
            prompt =prompt +" please make sure questions are medium difficulty ";
        }
        if(difficulty == 3) {
            prompt =prompt +" please make sure questions are hard difficulty ";
        }
        if(difficulty == 4) {
            prompt =prompt +" please make sure questions are related to subject but out of the box ";
        }
        return prompt;
    }

    public String buildQuestionsForTopic(String data, int difficulty){
        String prompt = "Can you build 10 multiple choice questions for the following topic -{ " + data + "} in Json format, the json should contain only these fields- questionId,questionText,answerChoices,correctAnswer\n";
        prompt = enhancePrompt(difficulty, prompt);
        return callLLM(prompt);
    }

    public String fixJson(String json){
        String prompt = "Can you fix the following json so that i can parse it properly - \n{{" + json + " }}\n";
        return callLLM(json);
    }

}
