package io.github.vishalmysore.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Log
@Component
public class IpValidator {

    @Value("${allowed.ip}")
    private String allowedIPs;

    private Set<String> allowedIpsSet;

    @PostConstruct
    public void init() {
        if (allowedIPs != null && !allowedIPs.isEmpty()) {
            allowedIpsSet = new HashSet<>(Arrays.asList(allowedIPs.split(",")));
        } else {
            allowedIpsSet = new HashSet<>();
        }
    }

    public boolean isAllowedIp(String ipAddress) {
        log.info("Receieved request from "+ipAddress);
        return allowedIpsSet.contains(ipAddress);
    }
}
