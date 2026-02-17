package mars.tripplanappbackend.common.config.auth.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    private final Key key;
    private final long validityInMilliseconds;

    public JwtProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long validityInMilliseconds) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public String createAccessToken(String usersId) {
        return createToken(usersId, validityInMilliseconds);
    }

    public String createRefreshToken() {
        long refreshTokenValidity = 14L * 24 * 60 * 60 * 1000;
        return createToken(null, refreshTokenValidity);
    }

    private String createToken(String subject, long validity) {
        Claims claims = Jwts.claims().setSubject(subject);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        String usersId = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();

        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(usersId, "", java.util.Collections.emptyList());
    }

    public boolean validateToken(String token, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            request.setAttribute("exception", "INVALID_TOKEN");
        } catch (ExpiredJwtException e) {
            request.setAttribute("exception", "EXPIRED_TOKEN");
        } catch (UnsupportedJwtException e) {
            request.setAttribute("exception", "UNSUPPORTED_TOKEN");
        } catch (IllegalArgumentException e) {
            request.setAttribute("exception", "EMPTY_TOKEN");
        } catch (Exception e) {
            request.setAttribute("exception", "UNKNOWN_ERROR");
        }
        return false;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}