package io.github.vishalmysore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private  String secretKey;

    @PostConstruct
    public void init() {
            secretKey = System.getenv("SECRET_KEY_JWT");
        }
    public  String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String getUserId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)  // Use the appropriate secret key
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();  // Assuming userId is stored in the "sub" field
    }
}
