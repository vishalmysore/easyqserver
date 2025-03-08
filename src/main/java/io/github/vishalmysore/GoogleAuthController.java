package io.github.vishalmysore;


import io.github.vishalmysore.data.AuthRequest;
import io.github.vishalmysore.data.GoogleUser;
import io.github.vishalmysore.security.JwtUtil;
import io.github.vishalmysore.service.GoogleAuthService;
import io.github.vishalmysore.service.base.GoogleDBService;
import io.github.vishalmysore.service.base.UserLoginDBSrvice;
import io.github.vishalmysore.service.dynamo.GoogleDynamoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@Controller
@RequestMapping("/auth")
public class GoogleAuthController {

    @Autowired
    private GoogleAuthService googleAuthService;
    @Autowired
    @Qualifier("userLoginDBService")
    private UserLoginDBSrvice userLoginDBService;

    @Autowired
    @Qualifier("googleDBService")
    private GoogleDBService googleDBService;
    @Autowired
    private JwtUtil jwtUtil;
    /**everting is done in filter you dont need to do anything**/


    @PostMapping("/google")
    public ResponseEntity<Map<String, String>> googleAuth(@RequestBody AuthRequest authRequest, HttpServletRequest request) {
        String jwtToken = authRequest.getJwtToken();
        boolean isNewUser = authRequest.isNewUser();

        GoogleUser googleUser = (GoogleUser)request.getAttribute("googleUser");
        String userId = null;
        if(googleUser == null) {
            return ResponseEntity.badRequest().build();
        }
        if(googleDBService.getGoogleUserByEmail(googleUser.getEmail()) == null) {
            userId = jwtUtil.getUserId(jwtToken);
            googleUser.setEasyQZUserId(userId);
            googleUser.setCreatedTimestamp(Instant.now().toString());
            googleDBService.insertGoogleUser(googleUser);
        } else {
            GoogleUser existingUser = googleDBService.getGoogleUserByEmail(googleUser.getEmail());
            userId = existingUser.getEasyQZUserId();
            googleDBService.updateLoginAndLogoutTime(googleUser.getEmail(),GoogleDynamoService.LOGIN_TIME);
        }
        userLoginDBService.makeUserPermanent(userId,googleUser.getEmail());
        String avtaar = userLoginDBService.getAvtaarByUserId(userId);
//        String userId = jwtUtil.getUserId(jwtToken);    //if user is not authenticated create a random and send
//        log.info("User ID: " + userId);
//        if(userId == null || userId.isEmpty()) {
//            Faker faker = new Faker();
//            userId  = faker.pokemon().name() + "_" + faker.animal().name() + "_" + faker.internet().domainWord();
//
//        } //else check in db for user matching this email form google

        String newjwtToken = jwtUtil.generateToken(userId);
        // Return response indicating user creation (You can customize the response as needed)
        Map<String, String> response = new HashMap<>();
        response.put("jwtToken", newjwtToken);
        response.put("userId", userId);
        response.put("avtaar", avtaar);
        response.put("emailid",googleUser.getEmail());
        return ResponseEntity.ok(response);
    }


}
