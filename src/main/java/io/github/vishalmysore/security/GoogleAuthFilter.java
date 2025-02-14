package io.github.vishalmysore.security;

import io.github.vishalmysore.data.GoogleUser;
import io.github.vishalmysore.service.GoogleAuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;

import java.io.IOException;

@Log
public class GoogleAuthFilter implements Filter {


    private final GoogleAuthService googleAuthService; // Assuming you have a service for Google SSO logic

    public GoogleAuthFilter(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Check if the request is targeting /auth/google
        String path = httpRequest.getRequestURI();
        if (path.startsWith("/auth/google")) {
            String token = httpRequest.getHeader("Authorization"); // Expecting token in Authorization header

            if (token != null && !token.isEmpty()) {
                try {
                    if (token.startsWith("Bearer ")) {
                        token = token.substring(7); // Remove the "Bearer " prefix (7 characters)
                    }
                    // Validate and fetch user details using the Google token
                    boolean isValidToken = googleAuthService.validateGoogleToken(token);
                    GoogleUser googleUser = googleAuthService.getUserInfo(token);
                    if (isValidToken) {
                        request.setAttribute("googleUser", googleUser); // Store the user details in the request
                        // Allow the request to continue if token is valid
                        log.info("Google token is valid.");
                        chain.doFilter(request, response);
                    } else {
                        // Reject the request if the token is invalid
                        log.info("Google token is not valid.");
                        response.getWriter().write("Access Denied: Invalid Google Token.");
                        response.getWriter().flush();
                    }
                } catch (Exception e) {
                    log.severe("Error validating Google token: " + e.getMessage());
                    response.getWriter().write("Access Denied: Error processing Google SSO.");
                    response.getWriter().flush();
                }
            } else {
                // Reject the request if no token is provided
                response.getWriter().write("Access Denied: No Google Token provided.");
                response.getWriter().flush();
            }
        } else {
            // Continue the filter chain if the path doesn't match /auth/google
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // Cleanup code, if necessary
    }
}
