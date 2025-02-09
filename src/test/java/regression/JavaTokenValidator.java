package regression;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Scanner;

public class JavaTokenValidator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Prompt the user for the access token
        System.out.print("Please enter your Google access token: ");
        String accessToken = scanner.nextLine(); // Replace with the actual access token you want to validate

        // URL to validate the token
        String validationUrl = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + accessToken;

        // Create a RestTemplate to send the HTTP request
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Send GET request to validate the token
            ResponseEntity<String> response = restTemplate.getForEntity(validationUrl, String.class);

            // If the request is successful (status 200), the response will contain token information
            if (response.getStatusCode().is2xxSuccessful()) {
                String tokenInfo = response.getBody();
                System.out.println("Token is valid! Token info: " + tokenInfo);
            } else {
                System.out.println("Token validation failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            // Handle error if the request fails or token is invalid
            System.out.println("Error validating token: " + e.getMessage());
        }
    }
}
