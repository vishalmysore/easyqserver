package io.github.vishalmysore;


import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;
@EnableCaching
@Configuration
public class CacheConfig {

    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.registerCustomCache("scrapedData",
                Caffeine.newBuilder()
                        .expireAfterWrite(24, TimeUnit.HOURS)  // 24 hours for scraped data
                        .maximumSize(500)
                        .build());

        cacheManager.registerCustomCache("stories",
                Caffeine.newBuilder()
                        .expireAfterWrite(30, TimeUnit.MINUTES)  // 1 hour for quiz results
                        .maximumSize(1000)
                        .build());

        return cacheManager;
    }
}
