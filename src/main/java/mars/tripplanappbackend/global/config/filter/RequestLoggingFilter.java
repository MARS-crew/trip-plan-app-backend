package mars.tripplanappbackend.global.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 전역 HTTP 요청 성능 모니터링 필터
 * * API 응답 속도가 지연되는 지점을 파악하기 위해 모든 요청의 실행 시간을 측정함
 * * 인터셉터 대신 필터를 사용하여 서블릿 컨텍스트 진입 시점부터의 완전한 흐름을 기록함
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 필터 체인의 시작과 끝을 측정하여 실제 비즈니스 로직 및 기타 보안 필터의 소요 시간을 포함함
        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long time = System.currentTimeMillis() - start;

            // 예외 응답도 누락하지 않고 상태 코드와 전체 요청 시간을 함께 남깁니다.
            log.info("[{}] {} status={} elapsedMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    time
            );
        }
    }
}
