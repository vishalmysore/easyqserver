package io.github.vishalmysore.service;


import io.github.vishalmysore.data.GoogleUser;
import io.github.vishalmysore.data.TokenInfo;
import io.github.vishalmysore.data.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
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

    // Method to exchange the authorization code for an access token
    public String getAccessTokenFromGoogle(String authorizationCode) {
        // Prepare the body for the token exchange request
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(GOOGLE_TOKEN_URL)
                .queryParam("code", authorizationCode)
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("grant_type", "authorization_code");

        // Send the POST request to Google API to exchange the authorization code for an access token
        RestTemplate restTemplate = new RestTemplate();
        TokenResponse tokenResponse = restTemplate.postForObject(uriBuilder.toUriString(), null, TokenResponse.class);

        if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
            return tokenResponse.getAccessToken();  // Return the access token
        } else {
            throw new RuntimeException("Failed to obtain access token from Google");
        }
    }

    // Example method to validate the token (optional, but useful)
    public boolean validateGoogleToken(String accessToken) {
        // You can make a request to Google's tokeninfo endpoint to validate the token if needed
        String validationUrl = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + accessToken;
        RestTemplate restTemplate = new RestTemplate();
        TokenInfo tokenInfo = restTemplate.getForObject(validationUrl, TokenInfo.class);

        return tokenInfo != null && tokenInfo.getAud().equals(clientId);  // Check if the token is valid
    }

    // You may also create a method to fetch user info using the access token
    public GoogleUser getUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        RestTemplate restTemplate = new RestTemplate();
        GoogleUser user = restTemplate.getForObject(userInfoUrl + "?access_token=" + accessToken, GoogleUser.class);

        return user;
    }

    // TokenResponse, TokenInfo, and GoogleUser are DTO classes for handling the responses
}
