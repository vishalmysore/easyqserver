package io.github.vishalmysore.security;

import io.github.vishalmysore.service.base.UserLoginDBSrvice;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@WebFilter("/*") // Apply the filter to all paths
public class UserLoginFilter implements Filter {


    private final UserLoginDBSrvice userLoginDBSrvice;
    private static final List<String> EXCLUDED_URLS = List.of("/api/createNewTempUser","/auth/google","/favicon.ico","/bs/");

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    public UserLoginFilter(@Qualifier("userLoginDBService")  UserLoginDBSrvice userLoginDBSrvice) {
        this.userLoginDBSrvice = userLoginDBSrvice;
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        log.info("Request URI: " + requestURI);
        if (EXCLUDED_URLS.stream().anyMatch(requestURI::contains)) {
            chain.doFilter(request, response); // Proceed without applying filter
            return;
        }
        String upgradeHeader = httpRequest.getHeader("Upgrade");
        if (upgradeHeader != null && upgradeHeader.equalsIgnoreCase("websocket")) {
            // This is a WebSocket request, bypass this filter
            log.info("Skipping filter for WebSocket connection");
            chain.doFilter(request, response); // Continue with the WebSocket request
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

            if(jwtUtil.isTokenExpired(token)){
                  String tempuserId = jwtUtil.getTempUserId(token);
                    log.warn("Token expired for user {}",tempuserId);
                    if(jwtUtil.isTempUser(token) && requestURI.equals("/api/markUserForRemoval")){
                        log.info("Temp user token expired {} and calling {} ",tempuserId,requestURI);
                        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(tempuserId, null, new ArrayList<>()));
                        chain.doFilter(request, response);
                        return;
                    } else {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired.");
                    }
                }
                String userId = jwtUtil.getUserId(token);
                // Assuming the user ID is stored in the "sub" field
                log.info("User ID from JWT: " + userId);
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>()));
                // Call the service to update the login data (user login, etc.)
              //  userLoginDBSrvice.trackUserLogin(userId,"temp", clientIp);

        } else {
            log.warn("No JWT token found in the request.");

            // Set Unauthorized status with a message instead of throwing an exception
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Token missing or expired.");
            return;
        }
        chain.doFilter(request, response);
    }


}
