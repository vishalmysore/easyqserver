package io.github.vishalmysore;

import io.github.vishalmysore.data.UserPerformance;
import io.github.vishalmysore.data.UserPerformanceData;
import io.github.vishalmysore.rag.RAGService;
import io.github.vishalmysore.service.LLMService;
import io.github.vishalmysore.service.base.UserAnalyticsDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
public class UserAnalyticsController {

    private static final long USER_PERFORMANCE_DATA_EXPIRY_MINUTES = 10;
    @Autowired
    private UserAnalyticsDBService userAnalyticsDBService;

    @Autowired
    private RAGService ragService;
    @Autowired
    private LLMService llmService;

    @PostMapping("/ragSearch")
    public ResponseEntity<List<String>> ragSearch(@RequestBody String query) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();  // Retrieve the userId set in the filter
        log.info("User {} queried for {}", userId, query);

        List<org.springframework.ai.document.Document> documents = ragService.getResult(query,userId);

        // Convert List<Document> to List<String>
        List<String> documentContents = documents.stream()
                .map(org.springframework.ai.document.Document::getText)
                .collect(Collectors.toList());

        return ResponseEntity.ok(documentContents);
    }


    @GetMapping("/getUserAnalytics")
    public UserPerformance getUserAnalytics(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();  // Retrieve the userId set in the filter
        UserPerformance performance =  userAnalyticsDBService.buildUserAnalytics(userId);
        return performance;
    }


    @PostMapping("/askLLMForUserAnalytics")
    public ResponseEntity<UserPerformanceData> askLLMForUserAnalytics(@RequestBody UserPerformance userPerformance) {
        UserPerformanceData data = userAnalyticsDBService.getUserPerformanceData(userPerformance.getUserId());
        if (data != null && data.getCreatedTime() != null) {
            // Get the current time in UTC
            Instant currentTime = Instant.now();

            // Check if the createdTime is within the last 10 minutes
            if (data.getCreatedTime().isAfter(currentTime.minus(Duration.ofMinutes(USER_PERFORMANCE_DATA_EXPIRY_MINUTES)))) {
                log.info("Data is recent (within 10 minutes), proceeding with cache {}...", data.getUserId());
                return ResponseEntity.ok(data);
            } else {
                log.info("Data is older than 10 minutes, creating new record for user. {}", data.getUserId());

            }
        }
        String llmResponse = llmService.callLLM("Here is user performance data "+JsonUtils.toJson(userPerformance)+ " can you Describe the user performance in Json Format {topSkills: [skill1, skill2], bottomSkills: [skill1, skill2], overallScore: ,improvements: [area1, area2],articlesToReadAgain:[ link:,specificReason:],textSummary: 'describe entire result in plain english do not miss anything'}");

        llmResponse = JsonUtils.fetchJson(llmResponse);
        Map<String,Object> filters = new HashMap<String,Object>();
        filters.put("userId",userPerformance.getUserId());
        filters.put("date", Date.from(Instant.now()).toString());
        ragService.addResult(llmResponse,filters);
        log.info("LLM Response for user analytics : "+llmResponse +" for user "+userPerformance.getUserId());
        data= JsonUtils.fromJson(llmResponse, UserPerformanceData.class);
        data.setUserId(userPerformance.getUserId());
        data.setAvatar(userPerformance.getAvatar());
        data.setEmailId(userPerformance.getEmailId());
        data.setVerified(userPerformance.isVerified());
        data.setCreatedTime(Instant.now());
        data.setArticles(userPerformance.getArticles());
        userAnalyticsDBService.updateUserPerformanceData(data);
        return ResponseEntity.ok(data);
    }


}
