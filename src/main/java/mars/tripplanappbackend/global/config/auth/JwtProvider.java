package mars.tripplanappbackend.global.config.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT 토큰의 생명주기 관리 및 유효성 검증을 담당합니다.
 */
@Component
public class JwtProvider {

    private final Key key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(
            @Value("${spring.jwt.secret}") String secretKey,
            @Value("${spring.jwt.accessTokenExpiration}") long accessTokenExpiration,
            @Value("${spring.jwt.refreshTokenExpiration}") long refreshTokenExpiration
    ) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String createAccessToken(String usersId, String email, String role) {
        return createToken(usersId, email, role, accessTokenExpiration);
    }

    /**
     * Access Token 재발급에 사용할 Refresh Token을 생성합니다.
     *
     * @return refresh token
     */
    public String createRefreshToken() {
        Date now = new Date();

        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String createToken(String usersId, String email, String role, long validity) {
        Claims claims = Jwts.claims().setSubject(usersId);
        claims.put("email", email);
        claims.put("role", role);

        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰의 클레임을 읽어 SecurityContext에 저장할 Authentication 객체를 생성합니다.
     *
     * @param token JWT access token
     * @return authentication
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String usersId = claims.getSubject();
        String email = claims.get("email", String.class);
        String role = claims.get("role", String.class);

        UserPrincipal userPrincipal = new UserPrincipal(usersId, email, role);

        var authorities = java.util.List.of(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role)
        );

        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userPrincipal, "", authorities
        );
    }

    /**
     * HTTP 요청 컨텍스트가 있는 경우 토큰 유효성을 검사하고 예외 원인을 request attribute로 남깁니다.
     *
     * @param token JWT token
     * @param request current request
     * @return validity
     */
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

    /**
     * HTTP 요청 컨텍스트 없이 사용할 단순 유효성 검사입니다.
     *
     * @param token JWT token
     * @return validity
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
