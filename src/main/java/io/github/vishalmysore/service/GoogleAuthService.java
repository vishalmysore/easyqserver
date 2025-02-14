package io.github.vishalmysore.service;


import io.github.vishalmysore.data.GoogleUser;
import io.github.vishalmysore.data.TokenInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
@Slf4j
@Service
public class GoogleAuthService {

    public GoogleAuthService() {
        super();
        log.info("GoogleAuthService constructor called");

    }

    @Value("${google_client_id}")
    private String clientId;

    @Value("${google_client_secret}")
    private String clientSecret;

    @Value("${google_redirect_uri}")
    private String redirectUri;

    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";



    public boolean validateGoogleToken(String accessToken) {
           String validationUrl = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + accessToken;
        RestTemplate restTemplate = new RestTemplate();
        TokenInfo tokenInfo = restTemplate.getForObject(validationUrl, TokenInfo.class);

        return tokenInfo != null && tokenInfo.getAud().equals(clientId);  // Check if the token is valid
    }


    public GoogleUser getUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        RestTemplate restTemplate = new RestTemplate();
        GoogleUser user = restTemplate.getForObject(userInfoUrl + "?access_token=" + accessToken, GoogleUser.class);

        return user;
    }

    // TokenResponse, TokenInfo, and GoogleUser are DTO classes for handling the responses
}
