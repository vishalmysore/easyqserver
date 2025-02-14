package io.github.vishalmysore.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LLMService {

    private final ChatLanguageModel chatModel;
    private final ChatLanguageModel paidChatModel;
    @Value("${paid_model_api_key}")
    private String paidModelApiKey;
    @Value("${use_paid_model:false}")
    private boolean usePaidModel = false;
    // Constructor to initialize the model once
    public LLMService(@Value("${openai_model:gpt-4o-mini-2024-07-18}") String modelName) {

        this.chatModel = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName(modelName)  // Make sure you provide the correct model name
                .build();
        if(usePaidModel) {
            this.paidChatModel = OpenAiChatModel.builder()
                    .apiKey(paidModelApiKey)
                    .modelName(modelName)  // Make sure you provide the correct model name
                    .build();
        } else {
            this.paidChatModel = null;
        }
        log.info("Model Initialized: " + modelName);
    }

    public String callLLM(String prompt){

        if (usePaidModel) {
            return callLLMWithPaidModel(prompt);
        } else {
            try {
                String result = chatModel.chat(prompt);

                log.info("Prompt: " + prompt);
                log.info("Result: " + result);

                return result;
            } catch (dev.ai4j.openai4j.OpenAiHttpException e) {
                if (e.getMessage().contains("please use your own OpenAI API key")) ;
                {
                    log.error("Please provide your own OpenAI API key in the environment variable OPENAI_API_KEY so trying again");
                    return callLLMWithPaidModel(prompt);
                }
            }
        }
    }

    public String callLLMWithPaidModel(String prompt){


            String result = paidChatModel.chat(prompt);

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
