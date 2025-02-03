package io.github.vishalmysore.security;

import io.github.vishalmysore.service.UserLoginDynamoService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Log
@Component
@WebFilter("/*")
public class UserLoginFilter implements Filter {


    private final UserLoginDynamoService userLoginDynamoService;
    private static final List<String> EXCLUDED_URLS = List.of("/api/createNewUser");

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    public UserLoginFilter(@Qualifier("userLoginDynamoService") UserLoginDynamoService userLoginDynamoService) {
        this.userLoginDynamoService = userLoginDynamoService;
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        if (EXCLUDED_URLS.stream().anyMatch(requestURI::contains)) {
            chain.doFilter(request, response); // Proceed without applying filter
            return;
        }
        String authorizationHeader = httpRequest.getHeader("Authorization");
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);  // Remove "Bearer " prefix
        }
        // Get Host from header
        String host = httpRequest.getHeader("Host");

        // Get IP from client
        String clientIp = httpRequest.getRemoteAddr();

        String ipAddress = httpRequest.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = httpRequest.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = httpRequest.getRemoteAddr(); // Fallback to direct request IP
        }
        log.info(" Host "+host);
        log.info("Ip "+ipAddress);
        log.info("Client IP "+clientIp);
        String origin = httpRequest.getHeader("Origin");
        String referer = httpRequest.getHeader("Referer");

        log.info("Origin: " + origin);
        log.info("Referer: " + referer);
        String forwardedFor = httpRequest.getHeader("X-Forwarded-For");
        log.info("Forwarded for "+forwardedFor);
        if (forwardedFor != null) {
            clientIp = forwardedFor.split(",")[0];  // Get the first IP in case of proxy chain
        }
        // Decode JWT token and extract user details if token is valid
        if (token != null) {

                String userId = jwtUtil.getUserId(token);  // Assuming the user ID is stored in the "sub" field
                log.info("User ID from JWT: " + userId);

                // Call the service to update the login data (user login, etc.)
                userLoginDynamoService.trackUserLogin(userId,"temp", clientIp);

        } else {
            log.warning("No JWT token found in the request.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token missing or expired.");
        }
        chain.doFilter(request, response);
    }
}
