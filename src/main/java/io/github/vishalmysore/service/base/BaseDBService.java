package io.github.vishalmysore.service.base;

import io.github.vishalmysore.data.Link;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface  BaseDBService {

    public  void saveOrUpdateLink(String url, String data) ;

    public   void createNewLink(String id, String url, String author, String data,String keywords) ;

    public  void updateLink(String id) ;

    public default String generateSHA256Hash(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(url.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }

    public abstract List<Link> getTrendingArticlesInLastHour() ;

    public abstract List<Link> getAllTimeTrendingArticles() ;
}
