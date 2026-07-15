package mars.tripplanappbackend.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.domain.PlaceTagMap;
import mars.tripplanappbackend.place.enums.PlaceType;
import mars.tripplanappbackend.place.repository.PlaceRepository;
import mars.tripplanappbackend.place.repository.PlaceTagMapRepository;
import mars.tripplanappbackend.search.domain.RecentSearch;
import mars.tripplanappbackend.search.domain.SearchCache;
import mars.tripplanappbackend.search.domain.SearchCachePlace;
import mars.tripplanappbackend.search.dto.request.DeleteAllRecentSearchRequestDto;
import mars.tripplanappbackend.search.dto.request.DeleteRecentSearchRequestDto;
import mars.tripplanappbackend.search.dto.request.PopularSearchListRequestDto;
import mars.tripplanappbackend.search.dto.request.RecentSearchListRequestDto;
import mars.tripplanappbackend.search.dto.request.SearchCategoryRequestDto;
import mars.tripplanappbackend.search.dto.request.SearchResultListRequestDto;
import mars.tripplanappbackend.search.dto.response.DeleteAllRecentSearchResponseDto;
import mars.tripplanappbackend.search.dto.response.DeleteRecentSearchResponseDto;
import mars.tripplanappbackend.search.dto.response.PopularSearchListResponseDto;
import mars.tripplanappbackend.search.dto.response.PopularSearchResponseDto;
import mars.tripplanappbackend.search.dto.response.RecentSearchListResponseDto;
import mars.tripplanappbackend.search.dto.response.RecentSearchResponseDto;
import mars.tripplanappbackend.search.dto.response.SearchCategoryListResponseDto;
import mars.tripplanappbackend.search.dto.response.SearchCategoryResponseDto;
import mars.tripplanappbackend.search.dto.response.SearchResultListResponseDto;
import mars.tripplanappbackend.search.dto.response.SearchResultResponseDto;
import mars.tripplanappbackend.search.enums.SearchCategory;
import mars.tripplanappbackend.search.event.SearchImageEnrichmentRequestedEvent;
import mars.tripplanappbackend.search.repository.PopularSearchKeywordProjection;
import mars.tripplanappbackend.search.repository.RecentSearchRepository;
import mars.tripplanappbackend.search.repository.SearchCachePlaceRepository;
import mars.tripplanappbackend.search.repository.SearchCacheRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 검색 화면에서 사용하는 전체 비즈니스 로직을 담당하는 서비스입니다.
 * 카테고리 조회, 검색 결과 조회, 최근 검색어/인기 검색어 처리,
 * 그리고 Google Places 결과를 내부 place 테이블과 동기화하는 역할을 함께 맡습니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    /**
     * 검색 결과 카드에 노출할 최대 태그 개수입니다.
     */
    private static final int MAX_TAG_COUNT = 3;

    /**
     * 최근 검색어 화면에 유지할 최대 활성 개수입니다.
     */
    private static final int MAX_RECENT_SEARCH_COUNT = 5;

    /**
     * Google 주소 파싱에서 국가를 찾지 못했을 때 사용할 기본값입니다.
     */
    private static final String DEFAULT_COUNTRY_NAME = "UNKNOWN";

    private static final int PLACE_NAME_MAX_LENGTH = 80;
    private static final int COUNTRY_NAME_MAX_LENGTH = 70;
    private static final int CITY_NAME_MAX_LENGTH = 90;
    private static final int ADDRESS_MAX_LENGTH = 255;
    private static final int OPENING_HOURS_MAX_LENGTH = 255;
    private static final int IMAGE_URL_MAX_LENGTH = 500;
    private static final int DESCRIPTION_MAX_LENGTH = 2000;

    private static final int SEARCH_CACHE_KEY_MAX_LENGTH = 150;
    /**
     * 오류를 정상 0건으로 저장하던 이전 캐시와 분리하기 위한 캐시 스키마 버전입니다.
     */
    private static final String SEARCH_CACHE_KEY_PREFIX = "gplaces-v4:";
    private static final int DEFAULT_SEARCH_PAGE_INDEX = 0;
    private static final int DEFAULT_SEARCH_PAGE_SIZE = 20;
    private static final int MAX_SEARCH_PAGE_SIZE = 20;
    private static final int MAX_SEARCH_RESULT_TARGET_COUNT = 100;
    private static final int GOOGLE_TEXT_SEARCH_PAGE_SIZE = 20;
    private static final int CATEGORY_NEARBY_RESULT_COUNT_PER_SEED = 10;
    private static final double CATEGORY_NEARBY_RADIUS_METERS = 20_000.0;
    private static final int NEARBY_EXPANSION_RESULT_COUNT_PER_GROUP = 5;
    private static final double NEARBY_EXPANSION_RADIUS_METERS = 5_000.0;
    private static final String GOOGLE_NEARBY_POPULARITY_RANK_PREFERENCE = "POPULARITY";
    private static final List<SearchSeedLocation> CATEGORY_NEARBY_SEED_LOCATIONS = List.of(
            new SearchSeedLocation(37.5665, 126.9780),
            new SearchSeedLocation(35.1796, 129.0756),
            new SearchSeedLocation(33.4996, 126.5312),
            new SearchSeedLocation(37.7519, 128.8761)
    );
    private static final List<List<String>> NEARBY_EXPANSION_INCLUDED_TYPE_GROUPS = List.of(
            List.of(
                    "tourist_attraction",
                    "historical_landmark",
                    "monument",
                    "beach",
                    "park",
                    "garden",
                    "museum",
                    "art_gallery",
                    "aquarium"
            ),
            List.of(
                    "restaurant",
                    "korean_restaurant",
                    "seafood_restaurant",
                    "cafe",
                    "bakery",
                    "bar"
            ),
            List.of(
                    "shopping_mall",
                    "department_store",
                    "market",
                    "gift_shop",
                    "store"
            ),
            List.of(
                    "hotel",
                    "resort_hotel",
                    "guest_house",
                    "hostel",
                    "lodging"
            )
    );

    private final MyPageRepository myPageRepository;
    private final PlaceRepository placeRepository;
    private final PlaceTagMapRepository placeTagMapRepository;
    private final RecentSearchRepository recentSearchRepository;
    private final SearchCacheRepository searchCacheRepository;
    private final SearchCachePlaceRepository searchCachePlaceRepository;
    private final GooglePlaceSearchService googlePlaceSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${search.performance.cold-cache-threshold-ms:5000}")
    private long coldCacheThresholdMs = 5_000L;

    /**
     * 검색 화면 상단에 노출하는 카테고리 목록을 정렬 순서대로 반환합니다.
     *
     * @param requestDto 검색 카테고리 조회 요청 DTO
     * @return 카테고리 목록 응답 DTO
     */
    public SearchCategoryListResponseDto getSearchCategories(SearchCategoryRequestDto requestDto) {
        List<SearchCategoryResponseDto> categories = Arrays.stream(SearchCategory.values())
                .sorted(Comparator.comparingInt(SearchCategory::getSortOrder))
                .map(SearchCategoryResponseDto::from)
                .toList();

        return SearchCategoryListResponseDto.of(categories);
    }

    /**
     * 검색 결과를 조회합니다.
     * 새 검색어이면 Google Places 결과를 place/search_cache 테이블에 저장한 뒤 응답하고,
     * 이전에 조회한 검색어이면 DB에 저장된 캐시 결과를 그대로 재사용합니다.
     *
     * @param requestDto 검색 결과 조회 요청 DTO
     * @return 검색 결과 목록 응답 DTO
     */
    @Transactional
    public SearchResultListResponseDto getSearchResults(SearchResultListRequestDto requestDto) {
        long startedAtNanos = System.nanoTime();
        String keyword = normalizeKeyword(requestDto.getKeyword());
        int page = normalizePage(requestDto.getPage());
        int size = normalizePageSize(requestDto.getSize());
        int requiredResultCount = calculateRequiredResultCount(page, size);
        String cacheKey = buildCacheKey(keyword);

        CachedSearch cachedSearch = findCachedSearch(cacheKey).orElse(null);
        boolean cacheHit = cachedSearch != null;
        List<Place> places;

        if (cachedSearch == null) {
            places = refreshSearchResults(keyword, cacheKey, null, requiredResultCount);
        } else {
            cachedSearch.searchCache().markSearched();
            places = cachedSearch.places();

            // 캐시 메타데이터와 실제 매핑 개수가 어긋난 경우에만 복구성 재동기화를 수행합니다.
            if (needsSearchCacheRefresh(cachedSearch, requiredResultCount)) {
                places = refreshSearchResultsSafely(
                        keyword,
                        cacheKey,
                        cachedSearch.searchCache(),
                        places,
                        requiredResultCount
                );
            }
        }

        places = filterSearchPlacesForKeyword(keyword, places);

        int totalCount = places.size();
        List<Place> pagedPlaces = slicePlaces(places, page, size);
        Map<Long, List<String>> tagsByPlaceId = getTagsByPlaceId(pagedPlaces);

        List<SearchResultResponseDto> searchResults = pagedPlaces.stream()
                .map(place -> {
                    List<String> tags = tagsByPlaceId.getOrDefault(place.getPlaceId(), List.of());
                    return SearchResultResponseDto.from(
                            place,
                            tags,
                            resolveSearchResultDescription(place, tags)
                    );
                })
                .toList();

        saveRecentSearchIfAuthenticated(requestDto.getUsersId(), keyword);

        SearchResultListResponseDto response =
                SearchResultListResponseDto.of(keyword, page, size, totalCount, searchResults);
        long elapsedMs = elapsedMillis(startedAtNanos);
        log.info(
                "Search request completed. keyword={}, cacheHit={}, page={}, resultCount={}, totalCount={}, elapsedMs={}",
                keyword,
                cacheHit,
                page,
                response.getResultCount(),
                response.getTotalCount(),
                elapsedMs
        );
        if (!cacheHit && elapsedMs > coldCacheThresholdMs) {
            log.warn(
                    "Cold-cache search exceeded target. keyword={}, targetMs={}, elapsedMs={}, totalCount={}",
                    keyword,
                    coldCacheThresholdMs,
                    elapsedMs,
                    response.getTotalCount()
            );
        }
        return response;
    }

    /**
     * 최근 검색어 누적 횟수를 기반으로 인기 검색어를 조회합니다.
     *
     * @param requestDto 인기 검색어 조회 요청 DTO
     * @return 인기 검색어 목록 응답 DTO
     */
    public PopularSearchListResponseDto getPopularSearches(PopularSearchListRequestDto requestDto) {
        List<PopularSearchKeywordProjection> popularKeywords = recentSearchRepository.findPopularSearchKeywords(
                PageRequest.of(0, requestDto.getLimit())
        );

        List<PopularSearchResponseDto> popularSearches = IntStream.range(0, popularKeywords.size())
                .mapToObj(index -> {
                    PopularSearchKeywordProjection projection = popularKeywords.get(index);
                    return PopularSearchResponseDto.of(
                            index + 1,
                            projection.getKeyword(),
                            projection.getSearchCount()
                    );
                })
                .toList();

        return PopularSearchListResponseDto.of(popularSearches);
    }

    /**
     * 로그인 사용자의 최근 검색어 목록을 최신순으로 조회합니다.
     *
     * @param requestDto 최근 검색어 조회 요청 DTO
     * @return 최근 검색어 목록 응답 DTO
     */
    public RecentSearchListResponseDto getRecentSearches(RecentSearchListRequestDto requestDto) {
        validateAuthenticatedUser(requestDto.getUsersId());

        List<RecentSearch> recentSearches = recentSearchRepository
                .findAllByUser_UsersIdAndIsDeletedFalseOrderByUpdatedAtDesc(
                        requestDto.getUsersId(),
                        PageRequest.of(0, requestDto.getLimit())
                );

        List<RecentSearchResponseDto> responseDtos = recentSearches.stream()
                .map(RecentSearchResponseDto::from)
                .toList();

        return RecentSearchListResponseDto.of(responseDtos);
    }

    /**
     * 선택한 최근 검색어 1건을 soft delete 처리합니다.
     *
     * @param requestDto 최근 검색어 삭제 요청 DTO
     * @return 삭제 결과 응답 DTO
     */
    @Transactional
    public DeleteRecentSearchResponseDto deleteRecentSearch(DeleteRecentSearchRequestDto requestDto) {
        validateAuthenticatedUser(requestDto.getUsersId());

        RecentSearch recentSearch = recentSearchRepository
                .findByRecentSearchIdAndUser_UsersIdAndIsDeletedFalse(
                        requestDto.getRecentSearchId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        recentSearch.markDeleted();
        return DeleteRecentSearchResponseDto.from(recentSearch);
    }

    /**
     * 로그인 사용자의 최근 검색어 목록 전체를 soft delete 처리합니다.
     *
     * @param requestDto 최근 검색어 전체 삭제 요청 DTO
     * @return 삭제 결과 응답 DTO
     */
    @Transactional
    public DeleteAllRecentSearchResponseDto deleteAllRecentSearches(DeleteAllRecentSearchRequestDto requestDto) {
        validateAuthenticatedUser(requestDto.getUsersId());

        List<RecentSearch> recentSearches =
                recentSearchRepository.findAllByUser_UsersIdAndIsDeletedFalse(requestDto.getUsersId());

        recentSearches.forEach(RecentSearch::markDeleted);
        return DeleteAllRecentSearchResponseDto.from(recentSearches.size());
    }

    /**
     * 정규화된 검색어로 저장된 검색 캐시를 조회합니다.
     * 캐시 본문과 연결된 장소 목록을 함께 묶어 반환합니다.
     *
     * @param cacheKey 정규화된 검색어 키
     * @return 캐시가 있으면 캐시 메타데이터와 장소 목록
     */
    private Optional<CachedSearch> findCachedSearch(String cacheKey) {
        return searchCacheRepository.findByKeyword(cacheKey)
                .map(searchCache -> new CachedSearch(searchCache, loadCachePlaces(searchCache)));
    }

    /**
     * Google 재동기화 중 예외가 발생해도 기존 캐시 결과를 그대로 응답하도록 보호하는 메서드입니다.
     *
     * @param keyword 원본 검색어
     * @param cacheKey 정규화된 검색어 키
     * @param existingCache 기존 검색 캐시
     * @param fallbackPlaces 재동기화 실패 시 사용할 기존 장소 목록
     * @return 최신 동기화 결과 또는 기존 캐시 결과
     */
    private List<Place> refreshSearchResultsSafely(
            String keyword,
            String cacheKey,
            SearchCache existingCache,
            List<Place> fallbackPlaces,
            int requiredResultCount
    ) {
        try {
            return refreshSearchResults(keyword, cacheKey, existingCache, requiredResultCount);
        } catch (RuntimeException exception) {
            log.warn("Cached search refresh failed. keyword={}, message={}", keyword, exception.getMessage());
            return fallbackPlaces;
        }
    }

    /**
     * Google Places 결과를 다시 읽어 place/search_cache를 갱신한 뒤
     * 최종적으로 화면에 반환할 장소 목록을 로드합니다.
     *
     * @param keyword 원본 검색어
     * @param cacheKey 정규화된 검색어 키
     * @param existingCache 기존 검색 캐시, 없으면 null
     * @return 정렬된 장소 목록
     */
    private List<Place> refreshSearchResults(
            String keyword,
            String cacheKey,
            SearchCache existingCache,
            int requiredResultCount
    ) {
        GoogleSyncResult syncResult = syncGooglePlaces(keyword, requiredResultCount);
        List<Place> searchPlaces = filterSearchPlacesForKeyword(
                keyword,
                deduplicateSearchPlaces(syncResult.places())
        );
        SearchCache searchCache = upsertSearchCache(cacheKey, existingCache, searchPlaces);
        publishImageEnrichmentRequested(syncResult.imageTasks());
        return loadCachePlaces(searchCache);
    }

    private void publishImageEnrichmentRequested(List<SearchImageEnrichmentRequestedEvent.Task> imageTasks) {
        if (imageTasks == null || imageTasks.isEmpty() || applicationEventPublisher == null) {
            return;
        }

        Map<Long, SearchImageEnrichmentRequestedEvent.Task> uniqueTasks = new LinkedHashMap<>();
        imageTasks.stream()
                .filter(Objects::nonNull)
                .filter(task -> task.placeId() != null && hasText(task.photoName()))
                .forEach(task -> uniqueTasks.putIfAbsent(task.placeId(), task));

        if (!uniqueTasks.isEmpty()) {
            applicationEventPublisher.publishEvent(
                    new SearchImageEnrichmentRequestedEvent(uniqueTasks.values().stream().toList())
            );
        }
    }

    private List<Place> filterSearchPlacesForKeyword(String keyword, List<Place> places) {
        if (isNullOrEmpty(places) || !isBeachKeyword(keyword.replaceAll("\\s+", ""))) {
            return places;
        }

        return places.stream()
                .filter(place -> !needsLocationRepair(place))
                .toList();
    }

    /**
     * 검색 캐시 메타데이터와 검색 결과-장소 매핑을 저장합니다.
     * 기존 캐시가 있으면 매핑을 전부 새로 갈아끼우고, 없으면 새 캐시를 생성합니다.
     *
     * @param cacheKey 정규화된 검색어 키
     * @param existingCache 기존 검색 캐시
     * @param places 검색 결과 장소 목록
     * @return 저장 후의 검색 캐시 엔티티
     */
    private SearchCache upsertSearchCache(String cacheKey, SearchCache existingCache, List<Place> places) {
        SearchCache searchCache = existingCache;

        if (searchCache == null) {
            searchCache = searchCacheRepository.save(SearchCache.create(cacheKey, places.size()));
        } else {
            searchCache.updateResults(places.size());
            searchCachePlaceRepository.deleteAllBySearchCache(searchCache);
        }

        if (places.isEmpty()) {
            return searchCache;
        }

        SearchCache targetCache = searchCache;
        List<SearchCachePlace> cachePlaces = IntStream.range(0, places.size())
                .mapToObj(index -> SearchCachePlace.create(targetCache, places.get(index), index))
                .toList();

        searchCachePlaceRepository.saveAll(cachePlaces);
        return searchCache;
    }

    /**
     * 검색 캐시에 연결된 장소 목록을 저장된 순서대로 조회합니다.
     * soft delete 된 place는 응답에서 제외합니다.
     *
     * @param searchCache 조회할 검색 캐시
     * @return 화면 응답에 사용할 장소 목록
     */
    private List<Place> loadCachePlaces(SearchCache searchCache) {
        return searchCachePlaceRepository.findAllBySearchCache_SearchCacheIdOrderBySortOrderAsc(searchCache.getSearchCacheId())
                .stream()
                .map(SearchCachePlace::getPlace)
                .filter(Objects::nonNull)
                .filter(place -> !Boolean.TRUE.equals(place.getIsDeleted()))
                .toList();
    }

    /**
     * 캐시 메타데이터와 실제 결과 매핑 개수가 어긋난 경우 재생성이 필요하다고 판단합니다.
     *
     * @param cachedSearch 캐시 메타데이터와 결과 목록 묶음
     * @return 캐시를 다시 구성해야 하면 true
     */
    private List<Place> slicePlaces(List<Place> places, int page, int size) {
        if (isNullOrEmpty(places)) {
            return List.of();
        }

        int fromIndex = page * size;
        if (fromIndex >= places.size()) {
            return List.of();
        }

        int toIndex = Math.min(fromIndex + size, places.size());
        return places.subList(fromIndex, toIndex);
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 0) {
            return DEFAULT_SEARCH_PAGE_INDEX;
        }
        return page;
    }

    private int normalizePageSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SEARCH_PAGE_SIZE;
        }
        return Math.min(size, MAX_SEARCH_PAGE_SIZE);
    }

    private int calculateRequiredResultCount(int page, int size) {
        long required = ((long) page + 1L) * size;
        return (int) Math.min(required, MAX_SEARCH_RESULT_TARGET_COUNT);
    }

    private boolean needsSearchCacheRefresh(CachedSearch cachedSearch, int requiredResultCount) {
        int cachedPlaceCount = cachedSearch.places().size();
        return cachedSearch.searchCache().getResultCount() != cachedSearch.places().size()
                || shouldExpandSearchCache(cachedPlaceCount, requiredResultCount);
    }

    private boolean shouldExpandSearchCache(int cachedPlaceCount, int requiredResultCount) {
        return cachedPlaceCount >= DEFAULT_SEARCH_PAGE_SIZE
                && cachedPlaceCount < requiredResultCount
                && cachedPlaceCount < MAX_SEARCH_RESULT_TARGET_COUNT;
    }

    private boolean needsLocationRepair(Place place) {
        return place != null
                && (!hasDisplayableLocationValue(place.getCityName())
                || !hasDisplayableLocationValue(place.getCountryName())
                || DEFAULT_COUNTRY_NAME.equalsIgnoreCase(place.getCountryName().trim()));
    }

    private boolean hasDisplayableLocationValue(String value) {
        if (!hasText(value)) {
            return false;
        }
        return !"null".equalsIgnoreCase(value.trim());
    }

    /**
     * 사용자가 입력한 검색어를 캐시 키 형태로 정규화합니다.
     * 대소문자와 중복 공백 차이로 동일 검색이 여러 캐시로 쪼개지는 것을 막습니다.
     *
     * @param keyword 원본 검색어
     * @return 정규화된 검색 캐시 키
     */
    private String buildCacheKey(String keyword) {
        String normalizedKeyword = keyword.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
        return truncate(SEARCH_CACHE_KEY_PREFIX + normalizedKeyword, SEARCH_CACHE_KEY_MAX_LENGTH);
    }

    /**
     * Google Places 검색 결과를 내부 place 테이블과 동기화합니다.
     * 결과가 처음 들어오면 신규 place를 만들고, 기존 place가 있으면 필요한 필드를 갱신합니다.
     *
     * @param keyword 검색어
     * @return 동기화된 장소 엔티티 목록
     */
    private GoogleSyncResult syncGooglePlaces(String keyword, int maxCount) {
        SearchCallMetrics metrics = new SearchCallMetrics();
        int candidateCount = 0;
        List<Place> syncedPlaces = new ArrayList<>();
        List<SearchImageEnrichmentRequestedEvent.Task> imageTasks = new ArrayList<>();

        try {
            List<GooglePlaceSearchService.GooglePlaceCandidate> googleCandidates =
                    collectGoogleSearchCandidates(keyword, maxCount, metrics);
            candidateCount = googleCandidates.size();

            List<PreparedGooglePlace> preparedPlaces = googleCandidates.stream()
                    .map(candidate -> prepareGooglePlace(candidate, metrics))
                    .filter(Objects::nonNull)
                    .toList();
            List<PendingSyncedGooglePlace> pendingPlaces = syncPreparedGooglePlaces(preparedPlaces);

            syncedPlaces.addAll(pendingPlaces.stream()
                    .map(PendingSyncedGooglePlace::place)
                    .toList());
            imageTasks.addAll(pendingPlaces.stream()
                    .filter(pending -> hasText(pending.firstPhotoName()))
                    .filter(pending -> !hasText(pending.place().getImageUrl()))
                    .filter(pending -> pending.place().getPlaceId() != null)
                    .map(pending -> new SearchImageEnrichmentRequestedEvent.Task(
                            pending.place().getPlaceId(),
                            pending.firstPhotoName()
                    ))
                    .toList());
            return new GoogleSyncResult(syncedPlaces, imageTasks);
        } finally {
            log.info(
                    "Google search sync completed. keyword={}, textCalls={}, nearbyCalls={}, detailsCalls={}, candidateCount={}, syncedCount={}, elapsedMs={}",
                    keyword,
                    metrics.textSearchCalls,
                    metrics.nearbySearchCalls,
                    metrics.detailsCalls,
                    candidateCount,
                    syncedPlaces.size(),
                    metrics.elapsedMillis()
            );
        }
    }

    private PreparedGooglePlace prepareGooglePlace(
            GooglePlaceSearchService.GooglePlaceCandidate candidate,
            SearchCallMetrics metrics
    ) {
        if (candidate == null || !hasText(candidate.googlePlaceId()) || !hasText(candidate.name())) {
            return null;
        }

        GooglePlaceSearchService.GooglePlaceCandidate enrichedCandidate =
                enrichCandidateWithDetails(candidate, metrics);
        String name = truncate(enrichedCandidate.name().trim(), PLACE_NAME_MAX_LENGTH);
        String address = truncate(nullableTrim(enrichedCandidate.formattedAddress()), ADDRESS_MAX_LENGTH);
        GoogleAddressParser.ParsedAddress parsedAddress = GoogleAddressParser.parse(
                address,
                enrichedCandidate.addressComponents()
        );
        String countryName = truncate(nullableTrim(parsedAddress.countryName()), COUNTRY_NAME_MAX_LENGTH);
        if (!hasText(countryName)) {
            countryName = DEFAULT_COUNTRY_NAME;
        }

        return new PreparedGooglePlace(
                enrichedCandidate,
                name,
                address,
                countryName,
                truncate(nullableTrim(parsedAddress.cityName()), CITY_NAME_MAX_LENGTH),
                normalizeCoordinate(enrichedCandidate.latitude()),
                normalizeCoordinate(enrichedCandidate.longitude()),
                normalizeGoogleRating(enrichedCandidate.rating(), enrichedCandidate.userRatingCount()),
                normalizeGoogleReviewCount(enrichedCandidate.userRatingCount())
        );
    }

    private List<PendingSyncedGooglePlace> syncPreparedGooglePlaces(List<PreparedGooglePlace> preparedPlaces) {
        if (isNullOrEmpty(preparedPlaces)) {
            return List.of();
        }

        Set<String> googlePlaceIds = preparedPlaces.stream()
                .map(prepared -> prepared.candidate().googlePlaceId())
                .filter(this::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> names = preparedPlaces.stream()
                .map(PreparedGooglePlace::name)
                .filter(this::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, Place> placesByGooglePlaceId = loadPlacesByGooglePlaceId(googlePlaceIds);
        Map<String, List<Place>> placesByName = placeRepository
                .findAllByNameInAndIsDeletedFalse(names)
                .stream()
                .collect(Collectors.groupingBy(Place::getName));

        Set<Long> claimedExistingPlaceIds = new LinkedHashSet<>();
        List<Place> newPlaces = new ArrayList<>();
        List<PendingSyncedGooglePlace> pendingPlaces = new ArrayList<>();

        for (PreparedGooglePlace prepared : preparedPlaces) {
            Place place = findExistingPreparedPlace(prepared, placesByGooglePlaceId, placesByName)
                    .filter(existing -> existing.getPlaceId() == null
                            || claimedExistingPlaceIds.add(existing.getPlaceId()))
                    .orElse(null);
            PlaceType placeType = resolvePlaceTypeForSync(prepared.candidate(), place);
            String description = resolveDescriptionForSync(place, prepared.candidate().editorialSummary());
            String openingHours = resolveOpeningHoursForSync(
                    place,
                    prepared.candidate().regularOpeningWeekdayDescriptions()
            );
            String imageUrl = resolveImageUrlForSync(place);

            if (place == null) {
                place = Place.builder()
                        .name(prepared.name())
                        .googlePlaceId(prepared.candidate().googlePlaceId())
                        .countryName(prepared.countryName())
                        .cityName(prepared.cityName())
                        .address(prepared.address())
                        .description(description)
                        .latitude(prepared.latitude())
                        .longitude(prepared.longitude())
                        .placeType(placeType)
                        .openingHours(openingHours)
                        .imageUrl(imageUrl)
                        .googleRatingAvg(prepared.googleRatingAvg())
                        .googleReviewCount(prepared.googleReviewCount())
                        .build();
                newPlaces.add(place);
            } else {
                place.updateFromGoogle(
                        prepared.candidate().googlePlaceId(),
                        prepared.name(),
                        prepared.countryName(),
                        prepared.cityName(),
                        prepared.address(),
                        prepared.latitude(),
                        prepared.longitude(),
                        placeType,
                        description,
                        openingHours,
                        imageUrl,
                        prepared.googleRatingAvg(),
                        prepared.googleReviewCount()
                );
            }

            pendingPlaces.add(new PendingSyncedGooglePlace(place, prepared.candidate().firstPhotoName()));
        }

        if (!newPlaces.isEmpty()) {
            placeRepository.saveAll(newPlaces);
        }
        return pendingPlaces;
    }

    private Map<String, Place> loadPlacesByGooglePlaceId(Set<String> googlePlaceIds) {
        if (googlePlaceIds.isEmpty()) {
            return Map.of();
        }

        Map<String, List<Place>> groupedPlaces = placeRepository
                .findAllByGooglePlaceIdInAndIsDeletedFalseOrderByUpdatedAtDescCreatedAtDescPlaceIdDesc(googlePlaceIds)
                .stream()
                .collect(Collectors.groupingBy(
                        Place::getGooglePlaceId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        Map<String, Place> canonicalPlaces = new LinkedHashMap<>();
        groupedPlaces.forEach((googlePlaceId, places) -> {
            canonicalPlaces.put(googlePlaceId, places.get(0));
            if (places.size() > 1) {
                log.warn(
                        "Duplicate active places found for googlePlaceId={}. Using latest placeId={}. duplicateCount={}",
                        googlePlaceId,
                        places.get(0).getPlaceId(),
                        places.size()
                );
            }
        });
        return canonicalPlaces;
    }

    private Optional<Place> findExistingPreparedPlace(
            PreparedGooglePlace prepared,
            Map<String, Place> placesByGooglePlaceId,
            Map<String, List<Place>> placesByName
    ) {
        Place placeByGoogleId = placesByGooglePlaceId.get(prepared.candidate().googlePlaceId());
        if (placeByGoogleId != null) {
            return Optional.of(placeByGoogleId);
        }

        List<Place> sameNamePlaces = placesByName.getOrDefault(prepared.name(), List.of());
        Optional<Place> placeByAddress = sameNamePlaces.stream()
                .filter(place -> hasText(prepared.address()))
                .filter(place -> Objects.equals(place.getAddress(), prepared.address()))
                .findFirst();
        if (placeByAddress.isPresent()) {
            return placeByAddress;
        }

        return sameNamePlaces.stream()
                .filter(place -> hasText(prepared.cityName()) && hasText(prepared.countryName()))
                .filter(place -> Objects.equals(place.getCityName(), prepared.cityName()))
                .filter(place -> Objects.equals(place.getCountryName(), prepared.countryName()))
                .findFirst();
    }

    private List<GooglePlaceSearchService.GooglePlaceCandidate> collectGoogleSearchCandidates(
            String keyword,
            int maxCount
    ) {
        return collectGoogleSearchCandidates(keyword, maxCount, new SearchCallMetrics());
    }

    private List<GooglePlaceSearchService.GooglePlaceCandidate> collectGoogleSearchCandidates(
            String keyword,
            int maxCount,
            SearchCallMetrics metrics
    ) {
        List<GooglePlaceSearchService.GooglePlaceCandidate> googleCandidates = new ArrayList<>();

        for (String searchQuery : buildGoogleSearchQueries(keyword)) {
            metrics.textSearchCalls++;
            List<GooglePlaceSearchService.GooglePlaceCandidate> searchResults =
                    googlePlaceSearchService.searchPlaces(searchQuery, GOOGLE_TEXT_SEARCH_PAGE_SIZE);

            if (isNullOrEmpty(searchResults)) {
                continue;
            }

            if (!supportsCategoryExpansion(keyword)) {
                searchResults = searchResults.stream()
                        .filter(candidate -> isRelevantGoogleCandidate(keyword, candidate))
                        .toList();
            }

            googleCandidates.addAll(searchResults);
            googleCandidates = new ArrayList<>(limitUniqueGoogleCandidates(googleCandidates, maxCount));

            if (googleCandidates.size() >= maxCount) {
                break;
            }
        }

        if (supportsCategoryExpansion(keyword) && googleCandidates.size() < maxCount) {
            appendCategoryNearbyCandidates(keyword, googleCandidates, maxCount, metrics);
        }

        if (supportsAnchorNearbyExpansion(keyword) && googleCandidates.size() < maxCount) {
            appendNearbyExpansionCandidates(googleCandidates, maxCount, metrics);
        }

        return limitUniqueGoogleCandidates(googleCandidates, maxCount);
    }

    private List<String> buildGoogleSearchQueries(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        LinkedHashSet<String> queries = new LinkedHashSet<>();

        addGoogleSearchQuery(queries, normalizedKeyword);
        if (supportsCategoryExpansion(normalizedKeyword)) {
            appendKeywordSpecificFallbackQueries(queries, normalizedKeyword);
            appendRegionalFallbackQueries(queries, normalizedKeyword);
        }

        return queries.stream().toList();
    }

    private void appendKeywordSpecificFallbackQueries(Set<String> queries, String keyword) {
        String compactKeyword = keyword.replaceAll("\\s+", "");

        if (compactKeyword.contains("한강")) {
            addGoogleSearchQuery(queries, "한강공원");
            addGoogleSearchQuery(queries, "한강 공원");
            addGoogleSearchQuery(queries, "한강공원 서울");
            addGoogleSearchQuery(queries, "한강 서울");
        }

        if (compactKeyword.contains("뚝섬")) {
            addGoogleSearchQuery(queries, "뚝섬 한강공원");
            addGoogleSearchQuery(queries, "뚝섬한강공원");
            addGoogleSearchQuery(queries, "뚝섬유원지");
            addGoogleSearchQuery(queries, "뚝섬 서울");
        }

        if (isTouristAttractionKeyword(compactKeyword)) {
            addGoogleSearchQuery(queries, "관광명소");
            addGoogleSearchQuery(queries, "서울 관광명소");
            addGoogleSearchQuery(queries, "부산 관광명소");
            addGoogleSearchQuery(queries, "제주 관광지");
            addGoogleSearchQuery(queries, "tourist attraction Korea");
        }

        if (isRestaurantKeyword(compactKeyword)) {
            addGoogleSearchQuery(queries, "음식점");
            addGoogleSearchQuery(queries, "서울 맛집");
            addGoogleSearchQuery(queries, "부산 맛집");
            addGoogleSearchQuery(queries, "제주 맛집");
            addGoogleSearchQuery(queries, "restaurant Korea");
        }

        if (isBeachKeyword(compactKeyword)) {
            addGoogleSearchQuery(queries, "해수욕장");
            addGoogleSearchQuery(queries, "부산 해수욕장");
            addGoogleSearchQuery(queries, "제주 해변");
            addGoogleSearchQuery(queries, "강릉 해변");
            addGoogleSearchQuery(queries, "beach Korea");
        }

        if (isAccommodationKeyword(compactKeyword)) {
            addGoogleSearchQuery(queries, "호텔");
            addGoogleSearchQuery(queries, "서울 숙소");
            addGoogleSearchQuery(queries, "부산 숙소");
            addGoogleSearchQuery(queries, "제주 숙소");
            addGoogleSearchQuery(queries, "lodging Korea");
        }

        if (isNatureKeyword(compactKeyword)) {
            addGoogleSearchQuery(queries, "자연 명소");
            addGoogleSearchQuery(queries, "국립공원");
            addGoogleSearchQuery(queries, "서울 자연 명소");
            addGoogleSearchQuery(queries, "제주 자연");
            addGoogleSearchQuery(queries, "nature attraction Korea");
        }

        if (isCultureKeyword(compactKeyword)) {
            addGoogleSearchQuery(queries, "문화시설");
            addGoogleSearchQuery(queries, "박물관");
            addGoogleSearchQuery(queries, "미술관");
            addGoogleSearchQuery(queries, "서울 문화 명소");
            addGoogleSearchQuery(queries, "cultural attraction Korea");
        }
    }

    private boolean isTouristAttractionKeyword(String compactKeyword) {
        if (!hasText(compactKeyword)) {
            return false;
        }

        String normalizedKeyword = compactKeyword.toLowerCase(Locale.ROOT);
        return normalizedKeyword.contains("관광지")
                || normalizedKeyword.contains("관광명소")
                || normalizedKeyword.contains("명소")
                || normalizedKeyword.contains("attraction")
                || normalizedKeyword.contains("landmark");
    }

    private boolean isRestaurantKeyword(String compactKeyword) {
        if (!hasText(compactKeyword)) {
            return false;
        }

        String normalizedKeyword = compactKeyword.toLowerCase(Locale.ROOT);
        return normalizedKeyword.contains("맛집")
                || normalizedKeyword.contains("음식점")
                || normalizedKeyword.contains("식당")
                || normalizedKeyword.contains("restaurant")
                || normalizedKeyword.contains("food");
    }

    private boolean isBeachKeyword(String compactKeyword) {
        if (!hasText(compactKeyword)) {
            return false;
        }

        String normalizedKeyword = compactKeyword.toLowerCase(Locale.ROOT);
        return normalizedKeyword.contains("해변")
                || normalizedKeyword.contains("해수욕장")
                || normalizedKeyword.contains("바다")
                || normalizedKeyword.contains("beach");
    }

    private boolean isAccommodationKeyword(String compactKeyword) {
        if (!hasText(compactKeyword)) {
            return false;
        }

        String normalizedKeyword = compactKeyword.toLowerCase(Locale.ROOT);
        return normalizedKeyword.contains("숙소")
                || normalizedKeyword.contains("호텔")
                || normalizedKeyword.contains("리조트")
                || normalizedKeyword.contains("lodging")
                || normalizedKeyword.contains("hotel");
    }

    private boolean isNatureKeyword(String compactKeyword) {
        if (!hasText(compactKeyword)) {
            return false;
        }

        String normalizedKeyword = compactKeyword.toLowerCase(Locale.ROOT);
        return normalizedKeyword.contains("자연")
                || normalizedKeyword.contains("공원")
                || normalizedKeyword.contains("산림")
                || normalizedKeyword.contains("nature")
                || normalizedKeyword.contains("park");
    }

    private boolean isCultureKeyword(String compactKeyword) {
        if (!hasText(compactKeyword)) {
            return false;
        }

        String normalizedKeyword = compactKeyword.toLowerCase(Locale.ROOT);
        return normalizedKeyword.contains("문화")
                || normalizedKeyword.contains("박물관")
                || normalizedKeyword.contains("미술관")
                || normalizedKeyword.contains("culture")
                || normalizedKeyword.contains("museum");
    }

    private boolean supportsCategoryExpansion(String keyword) {
        String compactKeyword = normalizeKeyword(keyword).replaceAll("\\s+", "");
        return compactKeyword.contains("한강")
                || compactKeyword.contains("뚝섬")
                || isTouristAttractionKeyword(compactKeyword)
                || isRestaurantKeyword(compactKeyword)
                || isBeachKeyword(compactKeyword)
                || isAccommodationKeyword(compactKeyword)
                || isNatureKeyword(compactKeyword)
                || isCultureKeyword(compactKeyword);
    }

    private boolean supportsAnchorNearbyExpansion(String keyword) {
        String compactKeyword = normalizeKeyword(keyword).replaceAll("\\s+", "");
        return compactKeyword.contains("한강") || compactKeyword.contains("뚝섬");
    }

    private boolean isRelevantGoogleCandidate(
            String keyword,
            GooglePlaceSearchService.GooglePlaceCandidate candidate
    ) {
        if (candidate == null) {
            return false;
        }

        String normalizedKeyword = normalizeForRelevanceCheck(keyword);
        if (!hasText(normalizedKeyword)) {
            return false;
        }

        String candidateText = normalizeForRelevanceCheck(String.join(
                " ",
                Objects.toString(candidate.name(), ""),
                Objects.toString(candidate.formattedAddress(), ""),
                Objects.toString(candidate.shortFormattedAddress(), "")
        ));
        return candidateText.contains(normalizedKeyword);
    }

    private String normalizeForRelevanceCheck(String value) {
        if (!hasText(value)) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private void appendRegionalFallbackQueries(Set<String> queries, String keyword) {
        if (!containsAny(keyword, "서울", "seoul")) {
            addGoogleSearchQuery(queries, keyword + " 서울");
        }

        if (!containsAny(keyword, "대한민국", "한국", "korea")) {
            addGoogleSearchQuery(queries, keyword + " 대한민국");
        }
    }

    private void addGoogleSearchQuery(Set<String> queries, String query) {
        if (!hasText(query)) {
            return;
        }

        String normalizedQuery = query.trim().replaceAll("\\s+", " ");
        if (!normalizedQuery.isEmpty()) {
            queries.add(normalizedQuery);
        }
    }

    private boolean containsAny(String value, String... candidates) {
        if (!hasText(value) || candidates == null || candidates.length == 0) {
            return false;
        }

        String normalizedValue = value.toLowerCase(Locale.ROOT);
        for (String candidate : candidates) {
            if (hasText(candidate) && normalizedValue.contains(candidate.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }

        return false;
    }

    private void appendCategoryNearbyCandidates(
            String keyword,
            List<GooglePlaceSearchService.GooglePlaceCandidate> googleCandidates,
            int maxCount,
            SearchCallMetrics metrics
    ) {
        List<String> includedTypes = resolveCategoryNearbyIncludedTypes(keyword);
        if (includedTypes.isEmpty() || googleCandidates.size() >= maxCount) {
            return;
        }

        Map<String, GooglePlaceSearchService.GooglePlaceCandidate> uniqueCandidates = new LinkedHashMap<>();
        googleCandidates.forEach(candidate -> putUniqueGoogleCandidate(uniqueCandidates, candidate));

        for (SearchSeedLocation seedLocation : CATEGORY_NEARBY_SEED_LOCATIONS) {
            if (uniqueCandidates.size() >= maxCount) {
                break;
            }

            List<GooglePlaceSearchService.GooglePlaceCandidate> nearbyCandidates =
                    searchNearbyPlacesSafely(
                            seedLocation.latitude(),
                            seedLocation.longitude(),
                            CATEGORY_NEARBY_RADIUS_METERS,
                            includedTypes,
                            CATEGORY_NEARBY_RESULT_COUNT_PER_SEED,
                            GOOGLE_NEARBY_POPULARITY_RANK_PREFERENCE,
                            metrics
                    );

            if (isNullOrEmpty(nearbyCandidates)) {
                continue;
            }

            for (GooglePlaceSearchService.GooglePlaceCandidate candidate : nearbyCandidates) {
                putUniqueGoogleCandidate(uniqueCandidates, candidate);
                if (uniqueCandidates.size() >= maxCount) {
                    break;
                }
            }
        }

        googleCandidates.clear();
        googleCandidates.addAll(uniqueCandidates.values().stream().limit(maxCount).toList());
    }

    private List<String> resolveCategoryNearbyIncludedTypes(String keyword) {
        String compactKeyword = normalizeKeyword(keyword).replaceAll("\\s+", "");

        if (isRestaurantKeyword(compactKeyword)) {
            return List.of(
                    "restaurant",
                    "korean_restaurant",
                    "seafood_restaurant",
                    "japanese_restaurant",
                    "cafe",
                    "bakery"
            );
        }
        if (isBeachKeyword(compactKeyword)) {
            return List.of("beach");
        }
        if (isTouristAttractionKeyword(compactKeyword)) {
            return List.of(
                    "tourist_attraction",
                    "historical_landmark",
                    "monument",
                    "museum",
                    "art_gallery",
                    "aquarium"
            );
        }
        if (isAccommodationKeyword(compactKeyword)) {
            return List.of("hotel", "resort_hotel", "guest_house", "hostel", "lodging");
        }
        if (isNatureKeyword(compactKeyword)) {
            return List.of("park", "national_park", "botanical_garden", "garden", "hiking_area");
        }
        if (isCultureKeyword(compactKeyword)) {
            return List.of("museum", "art_gallery", "cultural_center", "performing_arts_theater");
        }

        return List.of();
    }

    private void appendNearbyExpansionCandidates(
            List<GooglePlaceSearchService.GooglePlaceCandidate> googleCandidates,
            int maxCount,
            SearchCallMetrics metrics
    ) {
        Optional<GooglePlaceSearchService.GooglePlaceCandidate> anchorCandidate =
                findNearbyExpansionAnchor(googleCandidates);
        if (anchorCandidate.isEmpty()) {
            return;
        }

        List<List<GooglePlaceSearchService.GooglePlaceCandidate>> candidateBuckets =
                searchNearbyExpansionCandidateBuckets(anchorCandidate.get(), metrics);
        appendRoundRobinGoogleCandidates(googleCandidates, candidateBuckets, maxCount);
    }

    private Optional<GooglePlaceSearchService.GooglePlaceCandidate> findNearbyExpansionAnchor(
            List<GooglePlaceSearchService.GooglePlaceCandidate> googleCandidates
    ) {
        if (isNullOrEmpty(googleCandidates)) {
            return Optional.empty();
        }

        return googleCandidates.stream()
                .filter(this::hasCoordinate)
                .findFirst();
    }

    private List<List<GooglePlaceSearchService.GooglePlaceCandidate>> searchNearbyExpansionCandidateBuckets(
            GooglePlaceSearchService.GooglePlaceCandidate anchorCandidate,
            SearchCallMetrics metrics
    ) {
        List<List<GooglePlaceSearchService.GooglePlaceCandidate>> candidateBuckets = new ArrayList<>();

        for (List<String> includedTypes : NEARBY_EXPANSION_INCLUDED_TYPE_GROUPS) {
            List<GooglePlaceSearchService.GooglePlaceCandidate> nearbyCandidates =
                    searchNearbyPlacesSafely(
                            anchorCandidate.latitude(),
                            anchorCandidate.longitude(),
                            NEARBY_EXPANSION_RADIUS_METERS,
                            includedTypes,
                            NEARBY_EXPANSION_RESULT_COUNT_PER_GROUP,
                            GOOGLE_NEARBY_POPULARITY_RANK_PREFERENCE,
                            metrics
                    );

            if (!isNullOrEmpty(nearbyCandidates)) {
                candidateBuckets.add(nearbyCandidates);
            }
        }

        return candidateBuckets;
    }

    private List<GooglePlaceSearchService.GooglePlaceCandidate> searchNearbyPlacesSafely(
            double latitude,
            double longitude,
            double radiusMeters,
            List<String> includedTypes,
            int maxResultCount,
            String rankPreference,
            SearchCallMetrics metrics
    ) {
        metrics.nearbySearchCalls++;
        return googlePlaceSearchService.searchNearbyPlaces(
                latitude,
                longitude,
                radiusMeters,
                includedTypes,
                maxResultCount,
                rankPreference
        );
    }

    private void appendRoundRobinGoogleCandidates(
            List<GooglePlaceSearchService.GooglePlaceCandidate> targetCandidates,
            List<List<GooglePlaceSearchService.GooglePlaceCandidate>> candidateBuckets,
            int maxCount
    ) {
        if (isNullOrEmpty(candidateBuckets) || targetCandidates.size() >= maxCount) {
            return;
        }

        Map<String, GooglePlaceSearchService.GooglePlaceCandidate> uniqueCandidates = new LinkedHashMap<>();
        targetCandidates.forEach(candidate -> putUniqueGoogleCandidate(uniqueCandidates, candidate));

        int maxBucketSize = candidateBuckets.stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);

        for (int bucketIndex = 0; bucketIndex < maxBucketSize && uniqueCandidates.size() < maxCount; bucketIndex++) {
            for (List<GooglePlaceSearchService.GooglePlaceCandidate> candidateBucket : candidateBuckets) {
                if (bucketIndex >= candidateBucket.size()) {
                    continue;
                }
                putUniqueGoogleCandidate(uniqueCandidates, candidateBucket.get(bucketIndex));
                if (uniqueCandidates.size() >= maxCount) {
                    break;
                }
            }
        }

        targetCandidates.clear();
        targetCandidates.addAll(uniqueCandidates.values().stream().limit(maxCount).toList());
    }

    private List<GooglePlaceSearchService.GooglePlaceCandidate> limitUniqueGoogleCandidates(
            List<GooglePlaceSearchService.GooglePlaceCandidate> googleCandidates,
            int maxCount
    ) {
        if (isNullOrEmpty(googleCandidates)) {
            return List.of();
        }

        Map<String, GooglePlaceSearchService.GooglePlaceCandidate> uniqueCandidates = new LinkedHashMap<>();
        for (GooglePlaceSearchService.GooglePlaceCandidate candidate : googleCandidates) {
            putUniqueGoogleCandidate(uniqueCandidates, candidate);
            if (uniqueCandidates.size() >= maxCount) {
                break;
            }
        }

        return uniqueCandidates.values().stream().toList();
    }

    private void putUniqueGoogleCandidate(
            Map<String, GooglePlaceSearchService.GooglePlaceCandidate> uniqueCandidates,
            GooglePlaceSearchService.GooglePlaceCandidate candidate
    ) {
        String candidateKey = buildGoogleCandidateKey(candidate);
        if (!hasText(candidateKey)) {
            return;
        }

        uniqueCandidates.putIfAbsent(candidateKey, candidate);
    }

    private String buildGoogleCandidateKey(GooglePlaceSearchService.GooglePlaceCandidate candidate) {
        if (candidate == null) {
            return null;
        }

        String googlePlaceId = nullableTrim(candidate.googlePlaceId());
        if (hasText(googlePlaceId)) {
            return "google:" + googlePlaceId;
        }

        String name = nullableTrim(candidate.name());
        String address = nullableTrim(candidate.formattedAddress());
        if (!hasText(name) && !hasText(address)) {
            return null;
        }

        return ("fallback:" + Objects.toString(name, "") + "|" + Objects.toString(address, ""))
                .toLowerCase(Locale.ROOT);
    }

    private boolean hasCoordinate(GooglePlaceSearchService.GooglePlaceCandidate candidate) {
        return candidate != null && candidate.latitude() != null && candidate.longitude() != null;
    }

    /**
     * Text Search 응답에 메타데이터가 비어 있을 때만 Place Details를 추가 조회해 병합합니다.
     *
     * @param candidate Text Search 결과 1건
     * @return 메타데이터가 보강된 후보 객체
     */
    private GooglePlaceSearchService.GooglePlaceCandidate enrichCandidateWithDetails(
            GooglePlaceSearchService.GooglePlaceCandidate candidate,
            SearchCallMetrics metrics
    ) {
        if (candidate == null || !hasText(candidate.googlePlaceId())) {
            return candidate;
        }

        if (!needsDetailsFallback(candidate)) {
            return candidate;
        }

        metrics.detailsCalls++;
        GooglePlaceSearchService.GooglePlaceCandidate details =
                googlePlaceSearchService.getPlaceDetails(candidate.googlePlaceId());
        if (details == null) {
            return candidate;
        }

        return new GooglePlaceSearchService.GooglePlaceCandidate(
                firstNonBlank(details.googlePlaceId(), candidate.googlePlaceId()),
                firstNonBlank(details.name(), candidate.name()),
                firstNonBlank(details.formattedAddress(), candidate.formattedAddress()),
                firstNonBlank(details.shortFormattedAddress(), candidate.shortFormattedAddress()),
                details.latitude() != null ? details.latitude() : candidate.latitude(),
                details.longitude() != null ? details.longitude() : candidate.longitude(),
                details.rating() != null ? details.rating() : candidate.rating(),
                details.userRatingCount() != null ? details.userRatingCount() : candidate.userRatingCount(),
                isNullOrEmpty(details.addressComponents())
                        ? candidate.addressComponents()
                        : details.addressComponents(),
                firstNonBlank(details.editorialSummary(), candidate.editorialSummary()),
                isNullOrEmpty(details.regularOpeningWeekdayDescriptions())
                        ? candidate.regularOpeningWeekdayDescriptions()
                        : details.regularOpeningWeekdayDescriptions(),
                firstNonBlank(details.firstPhotoName(), candidate.firstPhotoName()),
                firstNonBlank(details.primaryType(), candidate.primaryType()),
                isNullOrEmpty(details.types()) ? candidate.types() : details.types()
        );
    }

    /**
     * Details API 보강이 필요한지 판단합니다.
     * 소개글, 영업시간, 사진, 주소 컴포넌트 중 하나라도 비어 있으면 보강 대상으로 봅니다.
     *
     * @param candidate Google Places 후보
     * @return Details 재조회가 필요하면 true
     */
    private boolean needsDetailsFallback(GooglePlaceSearchService.GooglePlaceCandidate candidate) {
        return !hasText(candidate.formattedAddress())
                || !hasCoordinate(candidate)
                || (isNullOrEmpty(candidate.addressComponents()) && !hasText(candidate.formattedAddress()));
    }

    /**
     * 소개글 저장 우선순위를 결정합니다.
     * Google 값이 있으면 그 값을 사용하고, 없으면 기존 DB 값을 유지합니다.
     *
     * @param place 기존 place 엔티티
     * @param googleDescription Google 소개글
     * @return 저장할 소개글
     */
    private String resolveDescriptionForSync(Place place, String googleDescription) {
        String normalizedGoogleValue = normalizeDescription(googleDescription);
        if (hasText(normalizedGoogleValue)) {
            return normalizedGoogleValue;
        }
        if (place != null) {
            return normalizeDescription(place.getDescription());
        }
        return null;
    }

    /**
     * 영업시간 저장 우선순위를 결정합니다.
     * Google 값이 없을 때만 기존 DB 값을 유지합니다.
     *
     * @param place 기존 place 엔티티
     * @param googleOpeningHours Google 영업시간 목록
     * @return 저장할 영업시간 문자열
     */
    private String resolveOpeningHoursForSync(Place place, List<String> googleOpeningHours) {
        String normalizedGoogleValue = normalizeOpeningHours(googleOpeningHours);
        if (hasText(normalizedGoogleValue)) {
            return normalizedGoogleValue;
        }
        if (place != null) {
            return normalizeOpeningHours(
                    hasText(place.getOpeningHours()) ? List.of(place.getOpeningHours()) : List.of()
            );
        }
        return null;
    }

    /**
     * 이미지 URL 저장 우선순위를 결정합니다.
     * 최초 검색 응답에서는 외부 Photo API를 호출하지 않고 기존 이미지만 유지합니다.
     * 신규 장소의 이미지는 트랜잭션 커밋 후 비동기 이벤트로 보강합니다.
     *
     * @param place 기존 place 엔티티
     * @return 저장할 이미지 URL
     */
    private String resolveImageUrlForSync(Place place) {
        if (place != null) {
            return truncate(nullableTrim(place.getImageUrl()), IMAGE_URL_MAX_LENGTH);
        }
        return null;
    }

    /**
     * 소개글을 저장 가능한 길이와 형식으로 정리합니다.
     *
     * @param description 원본 소개글
     * @return 정리된 소개글
     */
    private String normalizeDescription(String description) {
        return truncate(nullableTrim(description), DESCRIPTION_MAX_LENGTH);
    }

    /**
     * Google weekdayDescriptions 목록을 하나의 문자열로 합칩니다.
     * 중복과 공백을 정리한 뒤 DB 컬럼 길이에 맞게 잘라 저장합니다.
     *
     * @param openingHoursLines 영업시간 라인 목록
     * @return 정리된 영업시간 문자열
     */
    private String normalizeOpeningHours(List<String> openingHoursLines) {
        if (isNullOrEmpty(openingHoursLines)) {
            return null;
        }

        String normalizedValue = openingHoursLines.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(this::hasText)
                .distinct()
                .collect(Collectors.joining(" | "));

        return truncate(nullableTrim(normalizedValue), OPENING_HOURS_MAX_LENGTH);
    }

    /**
     * 앞값이 비어 있지 않으면 앞값을, 아니면 뒷값을 반환합니다.
     *
     * @param first 우선값
     * @param second 대체값
     * @return 선택된 문자열
     */
    private String firstNonBlank(String first, String second) {
        if (hasText(first)) {
            return first.trim();
        }
        if (hasText(second)) {
            return second.trim();
        }
        return null;
    }

    /**
     * 리스트가 null 이거나 비어 있는지 확인합니다.
     *
     * @param list 검사할 리스트
     * @return 비어 있으면 true
     */
    private boolean isNullOrEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * Google 응답 순서를 유지하면서 중복 place를 제거합니다.
     *
     * @param syncedPlaces 동기화된 place 목록
     * @return 중복 제거된 place 목록
     */
    private List<Place> deduplicateSearchPlaces(List<Place> syncedPlaces) {
        if (syncedPlaces.isEmpty()) {
            return List.of();
        }

        Map<Long, Place> placesById = new LinkedHashMap<>();
        for (Place place : syncedPlaces) {
            if (place == null || place.getPlaceId() == null || Boolean.TRUE.equals(place.getIsDeleted())) {
                continue;
            }
            placesById.putIfAbsent(place.getPlaceId(), place);
        }

        return placesById.values().stream().toList();
    }

    /**
     * 위도/경도 값을 DB 스케일에 맞게 정리합니다.
     *
     * @param coordinate 원본 좌표값
     * @return 소수점 7자리로 정규화된 좌표
     */
    private PlaceType resolvePlaceTypeForSync(
            GooglePlaceSearchService.GooglePlaceCandidate candidate,
            Place existingPlace
    ) {
        PlaceType mappedPlaceType = resolvePlaceTypeFromGoogle(candidate);
        if (mappedPlaceType != null) {
            return mappedPlaceType;
        }
        if (existingPlace != null) {
            return PlaceType.normalizeForAppCategory(existingPlace.getPlaceType());
        }
        return PlaceType.ATTRACTION;
    }

    private PlaceType resolvePlaceTypeFromGoogle(GooglePlaceSearchService.GooglePlaceCandidate candidate) {
        if (candidate == null) {
            return null;
        }

        PlaceType primaryTypeMatch = mapGoogleTypeToPlaceType(candidate.primaryType());
        if (primaryTypeMatch != null) {
            return primaryTypeMatch;
        }

        if (isNullOrEmpty(candidate.types())) {
            return null;
        }

        for (String type : candidate.types()) {
            PlaceType mappedType = mapGoogleTypeToPlaceType(type);
            if (mappedType != null) {
                return mappedType;
            }
        }

        return null;
    }

    private PlaceType mapGoogleTypeToPlaceType(String googleType) {
        if (!hasText(googleType)) {
            return null;
        }

        String normalizedType = googleType.trim().toLowerCase(Locale.ROOT);

        return switch (normalizedType) {
            case "lodging", "hotel", "motel", "resort_hotel", "hostel", "guest_house",
                    "bed_and_breakfast", "extended_stay_hotel", "rv_park" -> PlaceType.ACCOMMODATION;
            case "restaurant", "cafe", "bakery", "bar", "brunch_restaurant", "breakfast_restaurant",
                    "coffee_shop", "fast_food_restaurant", "hamburger_restaurant", "ice_cream_shop",
                    "japanese_restaurant", "korean_restaurant", "meal_delivery", "meal_takeaway",
                    "pizza_restaurant", "ramen_restaurant", "seafood_restaurant", "steak_house" ->
                    PlaceType.RESTAURANT;
            case "shopping_mall", "department_store", "supermarket", "convenience_store", "market",
                    "clothing_store", "book_store", "gift_shop", "souvenir_store", "store" -> PlaceType.SHOPPING;
            case "museum", "art_gallery", "cultural_center", "performing_arts_theater", "library",
                    "church", "hindu_temple", "mosque", "synagogue", "buddhist_temple", "shinto_shrine" ->
                    PlaceType.CULTURE;
            case "beach" -> PlaceType.BEACH;
            case "park", "national_park", "botanical_garden", "garden", "campground",
                    "hiking_area", "natural_feature" -> PlaceType.NATURE;
            case "tourist_attraction", "historical_landmark", "monument", "observation_deck",
                    "amusement_park", "aquarium", "visitor_center", "zoo" -> PlaceType.ATTRACTION;
            default -> {
                if (normalizedType.endsWith("_restaurant")
                        || normalizedType.endsWith("_cafe")
                        || normalizedType.endsWith("_bar")) {
                    yield PlaceType.RESTAURANT;
                }
                if (normalizedType.endsWith("_store")
                        || normalizedType.endsWith("_shop")
                        || normalizedType.endsWith("_market")) {
                    yield PlaceType.SHOPPING;
                }
                yield null;
            }
        };
    }

    private BigDecimal normalizeCoordinate(Double coordinate) {
        if (coordinate == null) {
            return null;
        }
        return BigDecimal.valueOf(coordinate).setScale(7, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeGoogleRating(Double rating, Integer userRatingCount) {
        if (rating == null || userRatingCount == null || userRatingCount <= 0) {
            return null;
        }
        return BigDecimal.valueOf(rating).setScale(1, RoundingMode.HALF_UP);
    }

    private Integer normalizeGoogleReviewCount(Integer userRatingCount) {
        if (userRatingCount == null || userRatingCount <= 0) {
            return null;
        }
        return userRatingCount;
    }

    /**
     * 문자열을 최대 길이에 맞게 자릅니다.
     *
     * @param value 원본 문자열
     * @param maxLength 허용 최대 길이
     * @return 잘린 문자열 또는 원본 문자열
     */
    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /**
     * 문자열의 앞뒤 공백을 제거하고, 비어 있으면 null로 바꿉니다.
     *
     * @param value 원본 문자열
     * @return 정리된 문자열
     */
    private String nullableTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 검색 결과 목록에 포함된 장소들의 태그를 placeId 기준으로 묶어 반환합니다.
     * 중복 태그는 제거하고 최대 3개까지만 유지합니다.
     *
     * @param places 검색 결과 장소 목록
     * @return placeId -> 태그 목록 맵
     */
    private Map<Long, List<String>> getTagsByPlaceId(List<Place> places) {
        if (places.isEmpty()) {
            return Map.of();
        }

        List<Long> placeIds = places.stream()
                .map(Place::getPlaceId)
                .toList();

        List<PlaceTagMap> placeTagMaps = placeTagMapRepository.findAllByPlace_PlaceIdInAndIsDeletedFalse(placeIds);

        return placeTagMaps.stream()
                .collect(Collectors.groupingBy(
                        placeTagMap -> placeTagMap.getPlace().getPlaceId(),
                        Collectors.mapping(
                                placeTagMap -> placeTagMap.getPlaceTag().getTagName(),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        tags -> tags.stream()
                                                .distinct()
                                                .limit(MAX_TAG_COUNT)
                                                .toList()
                                )
                        )
                ));
    }

    /**
     * 검색 결과 카드에 내려줄 소개글을 결정합니다.
     * 원본 description 이 있으면 그대로 사용하고, 없으면 화면 표시용 기본 문구를 생성합니다.
     *
     * @param place 검색 결과 place
     * @param tags 검색 결과 태그 목록
     * @return 검색 결과 카드 소개글
     */
    private String resolveSearchResultDescription(Place place, List<String> tags) {
        String savedDescription = nullableTrim(place.getDescription());
        if (hasText(savedDescription)) {
            return savedDescription;
        }

        return buildSearchResultFallbackDescription(place, tags);
    }

    /**
     * 검색 결과 전용 fallback 소개글을 생성합니다.
     * 지역/행정구역처럼 Google editorial summary 가 없는 결과도 description 이 null 로 내려가지 않게 보완합니다.
     *
     * @param place 검색 결과 place
     * @param tags 검색 결과 태그 목록
     * @return 화면 표시용 기본 소개글
     */
    private String buildSearchResultFallbackDescription(Place place, List<String> tags) {
        StringBuilder builder = new StringBuilder();
        String name = nullableTrim(place.getName());

        if (hasText(name)) {
            builder.append(name).append("에 대한 소개 정보가 아직 준비되지 않았습니다.");
        } else {
            builder.append("소개 정보가 아직 준비되지 않았습니다.");
        }

        String locationText = buildSearchResultLocationText(place);
        if (hasText(locationText)) {
            builder.append(" 위치: ").append(locationText).append('.');
        }

        String tagText = buildSearchResultTagText(tags);
        if (hasText(tagText)) {
            builder.append(" 대표 키워드: ").append(tagText).append('.');
        }

        return truncate(builder.toString(), DESCRIPTION_MAX_LENGTH);
    }

    /**
     * fallback 소개글에 표시할 위치 문구를 생성합니다.
     * country/city 를 우선 사용하고, 둘 다 없으면 상세 주소를 대체값으로 사용합니다.
     *
     * @param place 검색 결과 place
     * @return 위치 문구
     */
    private String buildSearchResultLocationText(Place place) {
        List<String> locationParts = new ArrayList<>();

        String countryName = nullableTrim(place.getCountryName());
        if (hasText(countryName) && !DEFAULT_COUNTRY_NAME.equalsIgnoreCase(countryName)) {
            locationParts.add(countryName);
        }

        String cityName = nullableTrim(place.getCityName());
        if (hasText(cityName)) {
            locationParts.add(cityName);
        }

        if (!locationParts.isEmpty()) {
            return String.join(" ", locationParts);
        }

        return nullableTrim(place.getAddress());
    }

    /**
     * fallback 소개글에 표시할 태그 문구를 생성합니다.
     *
     * @param tags 검색 결과 태그 목록
     * @return 태그 문구
     */
    private String buildSearchResultTagText(List<String> tags) {
        if (isNullOrEmpty(tags)) {
            return null;
        }

        return tags.stream()
                .map(this::nullableTrim)
                .filter(this::hasText)
                .distinct()
                .limit(MAX_TAG_COUNT)
                .collect(Collectors.joining(", "));
    }

    /**
     * 로그인 사용자라면 최근 검색어를 저장하거나 검색 횟수를 증가시킵니다.
     * 같은 검색어를 다시 검색하면 기존 row를 재사용하고, 최근 검색어 화면에서는 최대 5건만 활성 유지합니다.
     *
     * @param usersId 로그인 사용자 ID
     * @param keyword 검색어
     */
    private void saveRecentSearchIfAuthenticated(String usersId, String keyword) {
        User authenticatedUser = findAuthenticatedUser(usersId);
        if (authenticatedUser == null) {
            return;
        }

        List<RecentSearch> searchedKeywords =
                recentSearchRepository.findAllByUser_UsersIdAndKeywordOrderByUpdatedAtDesc(usersId, keyword);

        if (searchedKeywords.isEmpty()) {
            recentSearchRepository.save(RecentSearch.create(authenticatedUser, keyword));
        } else {
            RecentSearch latestSearch = searchedKeywords.get(0);
            latestSearch.increaseSearchCount();

            searchedKeywords.stream()
                    .skip(1)
                    .filter(recentSearch -> !recentSearch.getIsDeleted())
                    .forEach(RecentSearch::markDeleted);
        }

        List<RecentSearch> recentSearches =
                recentSearchRepository.findAllByUser_UsersIdAndIsDeletedFalseOrderByUpdatedAtDesc(usersId);

        if (recentSearches.size() <= MAX_RECENT_SEARCH_COUNT) {
            return;
        }

        recentSearches.stream()
                .skip(MAX_RECENT_SEARCH_COUNT)
                .forEach(RecentSearch::markDeleted);
    }

    /**
     * 공개 검색 API에서도 사용할 수 있도록, usersId가 없으면 예외 대신 null을 반환합니다.
     *
     * @param usersId 로그인 사용자 ID
     * @return 사용자 엔티티, 없으면 null
     */
    private User findAuthenticatedUser(String usersId) {
        if (usersId == null || usersId.isBlank()) {
            return null;
        }

        return myPageRepository.findByUsersIdAndIsDeletedFalse(usersId).orElse(null);
    }

    /**
     * 인증이 필요한 검색 부가 기능에서 사용자 존재 여부를 검증합니다.
     *
     * @param usersId 로그인 사용자 ID
     */
    private void validateAuthenticatedUser(String usersId) {
        if (usersId == null || usersId.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        myPageRepository.findByUsersIdAndIsDeletedFalse(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 문자열이 null 이 아니고 공백만으로 이루어지지 않았는지 확인합니다.
     *
     * @param value 검사할 문자열
     * @return 실제 텍스트가 있으면 true
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private long elapsedMillis(long startedAtNanos) {
        return Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
    }

    /**
     * 검색어 입력값을 검증하고 앞뒤 공백을 제거합니다.
     *
     * @param keyword 원본 검색어
     * @return 정리된 검색어
     */
    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        return keyword.trim();
    }

    /**
     * 검색 캐시 메타데이터와 실제 결과 목록을 함께 들고 다니기 위한 내부 record입니다.
     */
    private record CachedSearch(SearchCache searchCache, List<Place> places) {
    }

    private record GoogleSyncResult(
            List<Place> places,
            List<SearchImageEnrichmentRequestedEvent.Task> imageTasks
    ) {
    }

    private record PreparedGooglePlace(
            GooglePlaceSearchService.GooglePlaceCandidate candidate,
            String name,
            String address,
            String countryName,
            String cityName,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal googleRatingAvg,
            Integer googleReviewCount
    ) {
    }

    private record PendingSyncedGooglePlace(
            Place place,
            String firstPhotoName
    ) {
    }

    private static class SearchCallMetrics {

        private final long startedAtNanos = System.nanoTime();
        private int textSearchCalls;
        private int nearbySearchCalls;
        private int detailsCalls;

        private long elapsedMillis() {
            return Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
        }
    }

    private record SearchSeedLocation(double latitude, double longitude) {
    }
}
