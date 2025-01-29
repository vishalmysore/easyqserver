package io.github.vishalmysore;

import io.github.vishalmysore.service.LLMService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log
@RestController
public class EasyQContoller {

    @Autowired
    private LLMService llmService;

    @Aed

    @GetMapping("/getQuestions")
    public String getQuestions(@RequestParam("prompt") String prompt) {
     return llmService.callLLM(prompt);


    }
}
