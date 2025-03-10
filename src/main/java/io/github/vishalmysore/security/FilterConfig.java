package io.github.vishalmysore.security;

import io.github.vishalmysore.service.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Autowired
    private JwtUtil jwtUtil;

    @Bean
    public FilterRegistrationBean<HostFilter> loggingFilter(HostValidator hostValidator) {
        FilterRegistrationBean<HostFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new HostFilter(hostValidator));
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.addUrlPatterns("/bs/*");
        registrationBean.addUrlPatterns("/ws/*");
        registrationBean.addUrlPatterns("/wss/*");// Apply filter only to API endpoints
        return registrationBean;
    }
    @Bean
    public FilterRegistrationBean<GoogleAuthFilter> googleAuthFilter(GoogleAuthService googleAuthService) {
        FilterRegistrationBean<GoogleAuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new GoogleAuthFilter(googleAuthService)); // Register GoogleAuthFilter
        registrationBean.addUrlPatterns("/auth/google"); // Apply filter only to /auth/google
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<BSTokenFilter> broadCastFilter(GoogleAuthService googleAuthService) {
        FilterRegistrationBean<BSTokenFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new BSTokenFilter(jwtUtil)); // Register GoogleAuthFilter
        registrationBean.addUrlPatterns("/bs/*"); // Apply filter only to /auth/google
        return registrationBean;
    }
}
