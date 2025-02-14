package io.github.vishalmysore.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class BSTokenFilter implements Filter {


    private JwtUtil jwtUtil; // Ensure JwtUtil is a valid component

    public BSTokenFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    @Override
    public void doFilter(ServletRequest requestServer, ServletResponse responseServer, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) requestServer;
        HttpServletResponse response = (HttpServletResponse) responseServer;
        String uri = request.getRequestURI();
        log.info(" received request for {} ",  uri);
        String queryString = request.getQueryString();
        String token = extractToken(queryString);

        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            return;
        }

        String userId;
        try {
            userId = jwtUtil.getUserId(token);
        } catch (Exception e) {
            log.warn("Error validating token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Store the token and userId in request attributes for later use
        request.setAttribute("token", token);
        request.setAttribute("userId", userId);

        chain.doFilter(request, response);
    }

    private String extractToken(String queryString) {
        if (queryString == null || !queryString.contains("token=")) {
            return null;
        }

        return Optional.of(queryString)
                .map(q -> q.split("token="))
                .filter(parts -> parts.length > 1)
                .map(parts -> parts[1].split("&")[0])
                .orElse(null);
    }
}
