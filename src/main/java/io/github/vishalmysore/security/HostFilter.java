package io.github.vishalmysore.security;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;

import java.io.IOException;

@Log
@WebFilter("/*")
public class HostFilter implements Filter {

    private final HostValidator hostValidator;
    private final IpValidator ipValidator;

    // Constructor
    public HostFilter(HostValidator hostValidator, IpValidator ipValidator) {
        this.hostValidator = hostValidator;
        this.ipValidator = ipValidator;
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
        if (host != null && hostValidator.isAllowedHost(host) && clientIp != null && ipValidator.isAllowedIp(clientIp)) {
            // Allow the request to continue if both host and IP are valid
            chain.doFilter(request, response);
        } else {
            // Reject the request if either host or IP is not allowed
            response.getWriter().write("Access Denied: Invalid Host or IP.");
            response.getWriter().flush();
        }
    }

    @Override
    public void destroy() {
        // Cleanup code, if necessary
    }
}
