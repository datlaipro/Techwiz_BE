package tmtd.event.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, String roles, Long userId) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .claim("roles", roles)
                .claim("userId", userId) // Thêm userId vào token
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 1 ngày
                .signWith(key, Jwts.SIG.HS384)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String extractRoles(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("roles", String.class);
    }

    public Long extractUserId(String token) {
        Object v = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId"); // Lấy userId dưới dạng Long
        return (v == null) ? null : ((Number) v).longValue();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("[JwtUtil] Token expired at: " + e.getClaims().getExpiration());
        } catch (UnsupportedJwtException e) {
            System.out.println("[JwtUtil] Unsupported JWT: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("[JwtUtil] Malformed JWT: " + e.getMessage());
        } catch (SecurityException e) {
            System.out.println("[JwtUtil] Signature invalid: " + e.getMessage());
        } catch (JwtException e) {
            System.out.println("[JwtUtil] JWT error: " + e.getMessage());
        }
        return false;
    }
}
