package io.github.vishalmysore;

import io.github.vishalmysore.data.NewUser;
import io.github.vishalmysore.security.JwtUtil;
import io.github.vishalmysore.service.UserLoginDynamoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Log
public class UserController {
    @Autowired
    private UserLoginDynamoService userLoginDynamoService;

    @Autowired
    private JwtUtil jwtUtil;
    // POST method to create a new user
    @PostMapping("/createNewUser")
    public ResponseEntity<Map<String, String>> createNewUser(@RequestBody NewUser user, HttpServletRequest request) {
        String remoteIpAddress = request.getRemoteAddr();
        // Check if emailId is null, if yes, create a fake email address
        if (user.getEmailId() == null || user.getEmailId().isEmpty()) {
            String fakeEmail = user.getUserId() + "@easyqz.random";
            user.setEmailId(fakeEmail); // Set the generated email address
        }

        userLoginDynamoService.trackUserLogin(user.getUserId(), user.getEmailId(),remoteIpAddress); // Track user login
        log.info("User created or updated with Email: " + user.getEmailId());
        String jwtToken = jwtUtil.generateToken(user.getUserId());
        // Return response indicating user creation (You can customize the response as needed)
        Map<String, String> response = new HashMap<>();
        response.put("token", jwtToken);
        response.put("userId", user.getUserId());
        response.put("emailId", user.getEmailId());

        return ResponseEntity.ok(response);
    }
    }



