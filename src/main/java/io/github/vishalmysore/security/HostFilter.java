package io.github.vishalmysore.security;

import io.github.vishalmysore.service.AWSDynamoService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Log
public class HostFilter implements Filter {

    private final HostValidator hostValidator;

    @Autowired
    private AWSDynamoService awsDynamoService;

    // Constructor
    public HostFilter(HostValidator hostValidator) {
        this.hostValidator = hostValidator;

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

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


        // Check if the Host and IP are allowed
       if (origin != null && hostValidator.isAllowedHost(origin) ) {
            log.info("Host is allowed "+origin);
            // Allow the request to continue if both host and IP are valid
            chain.doFilter(request, response);
        } else {
           ((HttpServletResponse)response).setStatus(HttpServletResponse.SC_FORBIDDEN);
           // Reject the request if either host or IP is not allowed
            response.getWriter().write("Access Denied: Invalid Host or IP Bad Request.");
            response.getWriter().flush();
           response.getWriter().close();

        }


    }

    @Override
    public void destroy() {
        // Cleanup code, if necessary
    }
}
