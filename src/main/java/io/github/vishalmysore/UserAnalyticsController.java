package io.github.vishalmysore;

import io.github.vishalmysore.data.UserPerformance;
import io.github.vishalmysore.data.UserPerformanceData;
import io.github.vishalmysore.service.LLMService;
import io.github.vishalmysore.service.base.UserAnalyticsDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
public class UserAnalyticsController {

    @Autowired
    private UserAnalyticsDBService userAnalyticsDBService;

    @Autowired
    private LLMService llmService;

    @GetMapping("/getUserAnalytics")
    public UserPerformance getUserAnalytics(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();  // Retrieve the userId set in the filter
        return userAnalyticsDBService.getUserAnalytics(userId);
    }


    @PostMapping("/askLLMForUserAnalytics")
    public ResponseEntity<UserPerformanceData> askLLMForUserAnalytics(@RequestBody UserPerformance userPerformance) {
        String llmResponse = llmService.callLLM("Here is user performance data "+JsonUtils.toJson(userPerformance)+ " can you Describe the user performance in Json Format {topSkills: [skill1, skill2], bottomSkills: [skill1, skill2], overallScore: ,improvements: [area1, area2],articlesToReadAgain:[ link:,specificReason:],textSummary: 'describe what you think about this user here'}");
        llmResponse = JsonUtils.fetchJson(llmResponse);
        log.info("LLM Response for user analytics : "+llmResponse +" for user "+userPerformance.getUserId());
        UserPerformanceData data= JsonUtils.fromJson(llmResponse, UserPerformanceData.class);
        data.setUserId(userPerformance.getUserId());
        data.setAvatar(userPerformance.getAvatar());
        data.setEmailId(userPerformance.getEmailId());
        return ResponseEntity.ok(data);
    }


}
