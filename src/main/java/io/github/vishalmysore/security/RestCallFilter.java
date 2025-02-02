package io.github.vishalmysore.security;

import io.github.vishalmysore.service.UserLoginDynamoService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Log
@Component
@WebFilter("/*") // Apply the filter to all paths
public class RestCallFilter implements Filter {

    @Autowired
    private final UserLoginDynamoService userLoginDynamoService;

    @Autowired
    public RestCallFilter(@Qualifier("userLoginDynamoService") UserLoginDynamoService userLoginDynamoService) {
        this.userLoginDynamoService = userLoginDynamoService;
    }



    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Get details of the incoming request
        String restCallId = generateRestCallId(httpRequest);  // Generate a unique restCallId based on the request
        String method = httpRequest.getMethod();  // HTTP method (GET, POST, etc.)
        String uri = httpRequest.getRequestURI();  // Request URI (path)
        String ipAddress = httpRequest.getRemoteAddr();  // Client IP address
        String timestamp = Instant.now().toString();  // Current timestamp

        // Log the incoming request for debugging
        log.info("Received " + method + " request for " + uri + " from IP " + ipAddress);

        // Inject data into the usage table
        userLoginDynamoService.insertUsageData(restCallId, ipAddress, timestamp);  // Insert into DynamoDB

        // Proceed with the request
        chain.doFilter(request, response);  // Continue to the next filter or the endpoint
    }



    // Generate a unique restCallId based on the request method, URI, and timestamp
    private String generateRestCallId(HttpServletRequest httpRequest) {
        // Create a unique string by combining method, URI, and timestamp
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String timestamp = Instant.now().toString();

        // You can add additional components to make the ID even more unique (e.g., IP address)
        return method;
    }
}

