package io.github.vishalmysore.security;

import io.github.vishalmysore.service.base.UserLoginDBSrvice;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Component
@WebFilter("/*") // Apply the filter to all paths
public class RestCallFilter implements Filter {

    @Autowired
    @Qualifier("userLoginDBService")
    private final UserLoginDBSrvice userLoginDBSrvice;

    @Autowired
    public RestCallFilter(@Qualifier("userLoginDBService") UserLoginDBSrvice userLoginDBSrvice) {
        this.userLoginDBSrvice = userLoginDBSrvice;
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
        userLoginDBSrvice.insertUsageData(restCallId, ipAddress, timestamp)
                .thenAccept(totalUsed -> log.info("Usage Table TotalUsed count: {}, {}",restCallId, totalUsed))
                .exceptionally(ex -> {
                    log.error("Failed to insert/update usage data: {}", ex.getMessage());
                    return null;
                });  // Insert into DynamoDB

        // Proceed with the request
        chain.doFilter(request, response);  // Continue to the next filter or the endpoint
    }



    // Generate a unique restCallId based on the request method, URI, and timestamp
    private String generateRestCallId(HttpServletRequest httpRequest) {
        // Create a unique string by combining method, URI, and timestamp
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
       // String timestamp = Instant.now().toString();

        // You can add additional components to make the ID even more unique (e.g., IP address)
        return method+uri;
    }
}

