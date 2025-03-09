package io.github.vishalmysore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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
                .claim("tempUser", false)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * for temp user provide 24 hours to make a decision
     * @param userId
     * @return
     */
    public  String generateTokenForTempUser(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
               // .setExpiration(new Date(System.currentTimeMillis() + 1 * 30 * 1000)) // 1 minutes expiration for testing
                .claim("tempUser", true)
                .claim("tempUserId", userId)
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

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            // If the token is invalid or an exception occurs, return true
            return true;
        }
    }

    public boolean isTempUser(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Boolean isTempUser = (Boolean) claims.get("tempUser");
            return isTempUser;
        }  catch (ExpiredJwtException e) {
            // Extract claims even if the token is expired
            Claims expiredClaims = e.getClaims();
            return (boolean) expiredClaims.get("tempUser");
        }
        catch (Exception e) {
            // If the token is invalid or an exception occurs, return true
            return false;
        }
    }

    public String getTempUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return (String) claims.get("tempUserId");
        } catch (ExpiredJwtException e) {
            // Extract claims even if the token is expired
            Claims expiredClaims = e.getClaims();
            return (String) expiredClaims.get("tempUserId");
        } catch (Exception e) {
            return "false"; // Invalid token
        }
    }
}
