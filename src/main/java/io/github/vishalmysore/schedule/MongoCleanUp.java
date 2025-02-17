package io.github.vishalmysore.schedule;

import io.github.vishalmysore.data.mongo.DocumentLink;
import io.github.vishalmysore.data.mongo.LoginUser;
import io.github.vishalmysore.service.mongo.repo.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "easyQZ_DBTYPE", havingValue = "mongo", matchIfMissing = true)
public class MongoCleanUp {
    private final LoginUserRepository loginUserRepository;
    private final ArticleScoreRepository articleScoreRepository;
    private final StoryRepository storyRepository;
    private final UserScoreRepository userScoreRepository;

    private final LinkRepository linkRepository;

    @PostConstruct
    public void runAtStartup() {
        log.info("Running task at application startup: {}", LocalDateTime.now());
        executeTask();
    }

    // Runs daily at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void runDailyTask() {
        log.info("Running daily scheduled task at: {}", LocalDateTime.now());
        executeTask();
    }

    private void executeTask() {
        log.info("Executing Cleanup of temp data...");
        cleanupTempUsersAndScores();
        cleanupOldLinks();

    }

    @Transactional
    public void cleanupTempUsersAndScores() {
        // Fetch temp users who are not verified
        List<LoginUser> tempUsers = loginUserRepository.findByTempUserTrueAndVerifiedFalse();

        if (tempUsers.isEmpty()) {
            log.info("No temporary unverified users found.");
            return;
        }

        // Extract userIds
        List<String> userIds = tempUsers.stream()
                .map(LoginUser::getUserId)
                .collect(Collectors.toList());

        log.info("Deleting ArticleScores for users: {}", userIds);

        // Delete associated scores
        articleScoreRepository.deleteByUserIdIn(userIds);
        log.info("Deleting Stories for users: {}", userIds);
        storyRepository.deleteByUserIdIn(userIds);  // Delete stories too
        log.info("Deleting UserScores for users: {}", userIds);
        userScoreRepository.deleteByUserIdIn(userIds);
        log.info("Deleting temporary unverified users: {}", userIds);

        // Delete temp users
        loginUserRepository.deleteByUserIdIn(userIds);

        log.info("Cleanup complete.");
        log.info("Cleanup complete.");
    }

    @Transactional
    public void cleanupOldLinks() {
        // Calculate the threshold date (2 days ago)
        Instant sevenDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        Date thresholdDate = Date.from(sevenDaysAgo);

        // Find links older than 7 days
        List<DocumentLink> oldLinks = linkRepository.findByLastUsedBefore(thresholdDate);

        if (oldLinks.isEmpty()) {
            log.info("No old links found for deletion.");
            return;
        }

        // Extract IDs for logging
        List<String> linkIds = oldLinks.stream()
                .map(DocumentLink::getId)
                .collect(Collectors.toList());

        log.info("Deleting links last used before {}: {}", thresholdDate, linkIds);

        // Delete outdated links
        linkRepository.deleteAll(oldLinks);

        log.info("Old links cleanup completed.");
    }
}
