package io.github.vishalmysore.service.base;

import io.github.vishalmysore.data.Story;
import io.github.vishalmysore.service.LLMService;

public interface StoryDBService {
    public void insertStory(Story story);
    public String createStory(String prompt, LLMService llm);
}
