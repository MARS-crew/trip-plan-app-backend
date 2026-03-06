package mars.tripplanappbackend.global.config.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * JWT 기반 인증 필터
 * * HTTP 요청 시 헤더의 토큰을 검증하여 Spring Security의 인증 세션을 구성함
 * * OncePerRequestFilter를 상속받아 서블릿 포워딩 시 필터가 중복 실행되는 것을 방지함
 */
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        // 유효한 토큰이 확인되면 해당 요청의 생명주기 동안 사용할 인증 객체를 SecurityContext에 저장
        // 이후 컨트롤러나 서비스 레이어에서 @AuthenticationPrincipal 등으로 유저 정보를 참조하기 위함
        if (token != null && jwtProvider.validateToken(token, request)) {
            Authentication auth = jwtProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT 추출
     * * 'Bearer ' 접두사를 사용하는 토큰 형식을 지원함
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // "Bearer " 뒤의 실제 토큰 문자열만 추출 (인덱스 7부터)
            return bearerToken.substring(7);
        }
        return null;
    }
}