package io.github.vishalmysore;

import io.github.vishalmysore.data.NewUser;
import io.github.vishalmysore.security.JwtUtil;
import io.github.vishalmysore.service.base.UserLoginDBSrvice;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class UserController {
    @Autowired
    @Qualifier("userLoginDBService")
    private UserLoginDBSrvice userLoginDBSrvice;

    @Autowired
    private JwtUtil jwtUtil;
    // POST method to create a new user
    @PostMapping("/createNewTempUser")
    public ResponseEntity<Map<String, String>> createNewTempUser(@RequestBody NewUser user, HttpServletRequest request) {
        String remoteIpAddress = request.getRemoteAddr();
        // Check if emailId is null, if yes, create a fake email address
        if (user.getEmailId() == null || user.getEmailId().isEmpty()) {
            String fakeEmail = user.getUserId() + "@easyqz.random";
            user.setEmailId(fakeEmail); // Set the generated email address
        }

        boolean userCreated = userLoginDBSrvice.createTempUser(user.getUserId(), user.getEmailId(),remoteIpAddress,user.getAvatar()); // Track user login
        if(!userCreated) {
            return ResponseEntity.badRequest().build();
        }
        log.info("User created  with id {}: " ,user.getUserId());
        String jwtToken = jwtUtil.generateTokenForTempUser(user.getUserId());
        // Return response indicating user creation (You can customize the response as needed)
        Map<String, String> response = new HashMap<>();
        response.put("token", jwtToken);
        response.put("userId", user.getUserId());
        response.put("emailId", user.getEmailId());
        log.info("Jwt token generated for user: " +jwtToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/markUserForRemoval")
    public ResponseEntity<Map<String, String>> markUserForRemoval(HttpServletRequest request) {
        String remoteIpAddress = request.getRemoteAddr();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();
        log.info("Marking user for removal: " + userId);
        Map<String, String> response = new HashMap<>();
        response.put("userId", userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logoutGoogle")
    public ResponseEntity<Map<String, String>> logoutGoogle(HttpServletRequest request) {
        String remoteIpAddress = request.getRemoteAddr();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();
        log.info("Logged out Google User: " + userId);
        Map<String, String> response = new HashMap<>();
        response.put("userId", userId);
        return ResponseEntity.ok(response);
    }

  }



