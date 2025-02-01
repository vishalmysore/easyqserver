package io.github.vishalmysore.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Log
@Component
public class HostValidator {

    @Value("${allowedhosts}")
    private String allowedHosts;

    private String[] hostsArray;

    @PostConstruct
    public void init() {
        // Initialize the allowed hosts array after the value is injected
        if (allowedHosts != null) {
            hostsArray = allowedHosts.split(",");
        }
    }

    public boolean isAllowedHost(String host) {
        log.info("Received Request form host "+host);
        // Check if the host is present in the allowed hosts list
        if (hostsArray != null) {
            for (String allowedHost : hostsArray) {
                if (allowedHost.trim().equals(host)) {
                    return true;
                }
            }
        }
        return false;
    }
}

