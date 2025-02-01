package io.github.vishalmysore.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;
@Configuration
@EnableWebSecurity
public class SecurityConfig  implements WebMvcConfigurer {
    @Value("${allowedhosts}")
    private String allowedHosts;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .requestMatchers("/**").permitAll()  // Allow all requests without authentication
                .and()                 // Disable security context
                .csrf((csrf) -> csrf.disable()); // Disable CSRF protection

        return http.build();
    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow cross-origin requests from your frontend domain
        registry.addMapping("/**")
                .allowedOrigins(allowedHosts)  // Replace with your frontend URL
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

}

