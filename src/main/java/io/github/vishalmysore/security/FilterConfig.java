package io.github.vishalmysore.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<HostFilter> loggingFilter(HostValidator hostValidator,IpValidator ipValidator) {
        FilterRegistrationBean<HostFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new HostFilter(hostValidator,ipValidator));
        registrationBean.addUrlPatterns("/api/*"); // Apply filter only to API endpoints
        return registrationBean;
    }
}
