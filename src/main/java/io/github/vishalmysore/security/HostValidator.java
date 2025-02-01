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
            // Split the allowedHosts string into an array
            hostsArray = allowedHosts.split(",");

            // Print the allowed hosts and hosts array for debugging
            log.info("Allowed Hosts: " + allowedHosts);
            log.info("Hosts Array: ");
            for (String host : hostsArray) {
                log.info(host);
            }
        }
    }

    public boolean isAllowedHost(String host) {
        log.info("Received Request form host "+host);
        // Check if the host is present in the allowed hosts list
        if (hostsArray != null) {
            for (String allowedHost : hostsArray) {
                if (allowedHost.trim().equals(host)) {
                    log.info("Host is allowed");
                    return true;
                }
            }
        }
        return false;
    }
}

