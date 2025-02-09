package io.github.vishalmysore;


import com.github.javafaker.Faker;
import io.github.vishalmysore.data.AuthRequest;
import io.github.vishalmysore.security.JwtUtil;
import io.github.vishalmysore.service.GoogleAuthService;
import io.github.vishalmysore.service.UserLoginDynamoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@Controller
@RequestMapping("/auth")
public class GoogleAuthController {

    @Autowired
    private GoogleAuthService googleAuthService;
    @Autowired
    private UserLoginDynamoService userLoginDynamoService;

    @Autowired
    private JwtUtil jwtUtil;
    /**everting is done in filter you dont need to do anything**/

    @PostMapping("/google")
    public ResponseEntity<Map<String, String>> googleAuth(@RequestBody AuthRequest authRequest) {
        String jwtToken = authRequest.getJwtToken();
        boolean isNewUser = authRequest.isNewUser();

        String userId = jwtUtil.getUserId(jwtToken);    //if user is not authenticated create a random and send
        log.info("User ID: " + userId);
        if(userId == null || userId.isEmpty()) {
            Faker faker = new Faker();
            userId  = faker.pokemon().name() + "_" + faker.animal().name() + "_" + faker.internet().domainWord();

        } //else check in db for user matching this email form google

        String newjwtToken = jwtUtil.generateToken(userId);
        // Return response indicating user creation (You can customize the response as needed)
        Map<String, String> response = new HashMap<>();
        response.put("token", newjwtToken);
        response.put("userId", userId);

        return ResponseEntity.ok(response);
    }


}
