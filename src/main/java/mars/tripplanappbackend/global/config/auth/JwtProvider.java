package mars.tripplanappbackend.global.config.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT 토큰의 생명주기 관리 및 유효성 검증
 * * 클라이언트의 신원을 확인하기 위한 Access Token과
 * 세션 연장을 위한 Refresh Token의 발급 정책을 정의함
 */
@Component
public class JwtProvider {

    private final Key key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(
            @Value("${spring.jwt.secret}") String secretKey,
            @Value("${spring.jwt.accessTokenExpiration}") long accessTokenExpiration,
            @Value("${spring.jwt.refreshTokenExpiration}") long refreshTokenExpiration) {

        // HMAC-SHA 알고리즘에 적합한 키 규격을 보장하기 위해 Keys.hmacShaKeyFor 사용
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String createAccessToken(String usersId, String email, String role) {
        return createToken(usersId, email, role, accessTokenExpiration);
    }

    /**
     * Refresh Token 생성
     * Access Token 재발급을 위해 사용되며,
     * 유효기간은 application.yml의 spring.jwt.refreshTokenExpiration 설정값을 따른다.
     * 보안을 위해 payload에는 사용자 식별 정보를 포함하지 않는다.
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
     * 토큰 기반 인증 정보 추출
     * * SecurityContextHolder에 저장될 Authentication 객체를 생성하며,
     * 이후 필터 체인에서 권한 확인의 근거로 사용됨
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String usersId = claims.getSubject();
        String role = claims.get("role", String.class);

        var authorities = java.util.List.of(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role)
        );

        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                usersId, "", authorities
        );
    }
    /**
     * 토큰 유효성 검증 및 예외 원인 기록
     * * 검증 실패 시 단순히 false를 반환하는 대신,
     * Request 객체에 에러 타입을 담아 후속 필터나 엔트리포인트에서 상세한 응답을 보낼 수 있게 함
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

    // 내부 로직용(HTTP 요청 컨텍스트가 없는 경우) 단순 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}