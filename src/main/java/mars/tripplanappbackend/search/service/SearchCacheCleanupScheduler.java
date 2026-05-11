package mars.tripplanappbackend.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 오래된 검색 캐시를 주기적으로 정리하는 스케줄러입니다.
 * 검색만 하고 저장/위시리스트로 이어지지 않은 데이터가 무한히 쌓이지 않도록 관리합니다.
 */
@Component
@RequiredArgsConstructor
public class SearchCacheCleanupScheduler {

    /**
     * 검색 캐시 보관 개월 수입니다.
     * 기본값은 3개월이며 환경변수로 조정할 수 있습니다.
     */
    @Value("${search.cache.retention-months:3}")
    private int retentionMonths;

    private final SearchCacheCleanupService searchCacheCleanupService;

    /**
     * 설정된 cron 표현식에 따라 오래된 검색 캐시 정리를 실행합니다.
     */
    @Scheduled(cron = "${search.cache.cleanup-cron:0 30 3 * * *}")
    public void cleanupUnusedSearchCaches() {
        searchCacheCleanupService.cleanupUnusedSearchCaches(retentionMonths);
    }
}
