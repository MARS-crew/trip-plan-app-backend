package mars.tripplanappbackend.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mars.tripplanappbackend.mypage.repository.SavedPlaceRepository;
import mars.tripplanappbackend.search.domain.SearchCache;
import mars.tripplanappbackend.search.domain.SearchCachePlace;
import mars.tripplanappbackend.search.repository.SearchCachePlaceRepository;
import mars.tripplanappbackend.search.repository.SearchCacheRepository;
import mars.tripplanappbackend.trip.repository.WishlistPlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 오래된 검색 캐시를 정리하는 서비스입니다.
 * 단, 이미 저장한 장소나 위시리스트에서 사용 중인 장소가 포함된 캐시는
 * 검색 이력만 오래되었더라도 보존해 다른 기능에 영향을 주지 않도록 합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SearchCacheCleanupService {

    private final SearchCacheRepository searchCacheRepository;
    private final SearchCachePlaceRepository searchCachePlaceRepository;
    private final SavedPlaceRepository savedPlaceRepository;
    private final WishlistPlaceRepository wishlistPlaceRepository;

    /**
     * 보관 기간이 지난 검색 캐시 중 더 이상 참조되지 않는 캐시를 삭제합니다.
     *
     * @param retentionMonths 보관 개월 수
     * @return 삭제한 검색 캐시 개수
     */
    @Transactional
    public int cleanupUnusedSearchCaches(int retentionMonths) {
        // 0 이하가 들어와도 최소 1개월은 보관하도록 보정합니다.
        int normalizedRetentionMonths = Math.max(retentionMonths, 1);
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(normalizedRetentionMonths);
        List<SearchCache> expiredCaches = searchCacheRepository.findAllByLastSearchedAtBefore(cutoff);

        int deletedCount = 0;
        for (SearchCache searchCache : expiredCaches) {
            List<SearchCachePlace> cachePlaces =
                    searchCachePlaceRepository.findAllBySearchCache_SearchCacheIdOrderBySortOrderAsc(
                            searchCache.getSearchCacheId()
                    );

            List<Long> placeIds = cachePlaces.stream()
                    .map(SearchCachePlace::getPlace)
                    .filter(java.util.Objects::nonNull)
                    .map(place -> place.getPlaceId())
                    .distinct()
                    .toList();

            // 저장한 장소나 위시리스트에서 이미 사용 중인 장소가 하나라도 있으면 캐시를 유지합니다.
            if (isLinkedToSavedOrWishlist(placeIds)) {
                continue;
            }

            // 참조되지 않는 검색 캐시는 매핑부터 지우고 캐시 본문을 삭제합니다.
            searchCachePlaceRepository.deleteAll(cachePlaces);
            searchCacheRepository.delete(searchCache);
            deletedCount++;
        }

        if (deletedCount > 0) {
            log.info("Deleted {} stale search cache entries older than {} months.", deletedCount, normalizedRetentionMonths);
        }

        return deletedCount;
    }

    /**
     * 해당 장소 목록이 저장한 장소 또는 위시리스트에 연결되어 있는지 확인합니다.
     *
     * @param placeIds 검색 캐시에 포함된 장소 PK 목록
     * @return 하나라도 연결되어 있으면 true
     */
    private boolean isLinkedToSavedOrWishlist(List<Long> placeIds) {
        if (placeIds.isEmpty()) {
            return false;
        }

        return savedPlaceRepository.existsByPlace_PlaceIdInAndIsDeletedFalse(placeIds)
                || wishlistPlaceRepository.existsByPlace_PlaceIdInAndIsDeletedFalse(placeIds);
    }
}
