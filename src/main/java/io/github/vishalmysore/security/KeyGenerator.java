package io.github.vishalmysore.security;

import lombok.extern.java.Log;

import java.security.SecureRandom;

@Log
public class KeyGenerator {
    public static void main(String[] args) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[32]; // 256 bits = 32 bytes
        secureRandom.nextBytes(key);
        StringBuilder keyString = new StringBuilder();
        for (byte b : key) {
            keyString.append(String.format("%02x", b));
        }
        log.info(keyString.toString());
    }
}