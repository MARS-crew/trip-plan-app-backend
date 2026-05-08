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
import mars.tripplanappbackend.search.repository.PopularSearchKeywordProjection;
import mars.tripplanappbackend.search.repository.RecentSearchRepository;
import mars.tripplanappbackend.search.repository.SearchCachePlaceRepository;
import mars.tripplanappbackend.search.repository.SearchCacheRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private static final String SEARCH_CACHE_KEY_PREFIX = "gplaces-v2:";
    private static final int SEARCH_RESULT_TARGET_COUNT = 12;
    private static final int NEARBY_EXPANSION_RESULT_COUNT_PER_GROUP = 5;
    private static final double NEARBY_EXPANSION_RADIUS_METERS = 5_000.0;
    private static final String GOOGLE_NEARBY_POPULARITY_RANK_PREFERENCE = "POPULARITY";
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
        String keyword = normalizeKeyword(requestDto.getKeyword());
        String cacheKey = buildCacheKey(keyword);

        CachedSearch cachedSearch = findCachedSearch(cacheKey).orElse(null);
        List<Place> places;

        if (cachedSearch == null) {
            places = refreshSearchResults(keyword, cacheKey, null);
        } else {
            cachedSearch.searchCache().markSearched();
            places = cachedSearch.places();

            // 캐시 메타데이터와 실제 매핑 개수가 어긋난 경우에만 복구성 재동기화를 수행합니다.
            if (needsSearchCacheRefresh(cachedSearch)) {
                places = refreshSearchResultsSafely(keyword, cacheKey, cachedSearch.searchCache(), places);
            } else {
                repairMissingPlaceImages(places);
            }
        }

        Map<Long, List<String>> tagsByPlaceId = getTagsByPlaceId(places);

        List<SearchResultResponseDto> searchResults = places.stream()
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

        return SearchResultListResponseDto.of(keyword, searchResults);
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
            List<Place> fallbackPlaces
    ) {
        try {
            return refreshSearchResults(keyword, cacheKey, existingCache);
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
    private List<Place> refreshSearchResults(String keyword, String cacheKey, SearchCache existingCache) {
        List<Place> searchPlaces = deduplicateSearchPlaces(syncGooglePlaces(keyword));
        SearchCache searchCache = upsertSearchCache(cacheKey, existingCache, searchPlaces);
        return loadCachePlaces(searchCache);
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
    private boolean needsSearchCacheRefresh(CachedSearch cachedSearch) {
        return cachedSearch.searchCache().getResultCount() != cachedSearch.places().size();
    }

    /**
     * 검색 캐시에 연결된 place 중 대표 이미지가 비어 있으면 Google Place Details/Photo API로 한 번 더 보강합니다.
     * 예전에 imageUrl 없이 저장된 데이터를 같은 검색어 재조회만으로 자연스럽게 복구하기 위한 보정 로직입니다.
     *
     * @param places 검색 결과 place 목록
     */
    private void repairMissingPlaceImages(List<Place> places) {
        if (places == null || places.isEmpty()) {
            return;
        }

        places.stream()
                .filter(this::needsImageRepair)
                .forEach(this::repairPlaceImageFromGoogle);
    }

    /**
     * place 대표 이미지가 비어 있고, Google Place ID가 남아 있어 재조회가 가능한지 확인합니다.
     *
     * @param place 검색 결과 place
     * @return Google 이미지 재조회가 필요하면 true
     */
    private boolean needsImageRepair(Place place) {
        return place != null
                && !hasText(place.getImageUrl())
                && hasText(place.getGooglePlaceId());
    }

    /**
     * 단일 place의 대표 이미지를 Google Place Details/Photo API로 다시 조회해 채웁니다.
     * Google 쪽에도 사진이 없거나 photoUri 조회가 실패하면 기존처럼 null을 유지합니다.
     *
     * @param place 대표 이미지 복구 대상 place
     */
    private void repairPlaceImageFromGoogle(Place place) {
        GooglePlaceSearchService.GooglePlaceCandidate details =
                googlePlaceSearchService.getPlaceDetails(place.getGooglePlaceId());
        if (details == null || !hasText(details.firstPhotoName())) {
            log.debug("검색 캐시 이미지 보강 건너뜀 - 사진 메타데이터 없음. placeId={}, googlePlaceId={}",
                    place.getPlaceId(),
                    place.getGooglePlaceId());
            return;
        }

        String photoUri = googlePlaceSearchService.getPhotoUri(details.firstPhotoName());
        if (!hasText(photoUri)) {
            log.debug("검색 캐시 이미지 보강 실패 - photoUri 조회 실패. placeId={}, googlePlaceId={}",
                    place.getPlaceId(),
                    place.getGooglePlaceId());
            return;
        }

        place.updateImageUrlFromGoogle(truncate(nullableTrim(photoUri), IMAGE_URL_MAX_LENGTH));
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
    private List<Place> syncGooglePlaces(String keyword) {
        List<GooglePlaceSearchService.GooglePlaceCandidate> googleCandidates =
                collectGoogleSearchCandidates(keyword);

        if (googleCandidates.isEmpty()) {
            return List.of();
        }

        List<Place> syncedPlaces = new ArrayList<>();
        for (GooglePlaceSearchService.GooglePlaceCandidate candidate : googleCandidates) {
            Place syncedPlace = syncSingleGooglePlace(candidate);
            if (syncedPlace != null) {
                syncedPlaces.add(syncedPlace);
            }
        }
        return syncedPlaces;
    }

    private List<GooglePlaceSearchService.GooglePlaceCandidate> collectGoogleSearchCandidates(String keyword) {
        List<GooglePlaceSearchService.GooglePlaceCandidate> googleCandidates = new ArrayList<>(
                limitUniqueGoogleCandidates(
                        googlePlaceSearchService.searchPlaces(keyword),
                        SEARCH_RESULT_TARGET_COUNT
                )
        );

        if (googleCandidates.size() < SEARCH_RESULT_TARGET_COUNT) {
            appendNearbyExpansionCandidates(googleCandidates);
        }

        return limitUniqueGoogleCandidates(googleCandidates, SEARCH_RESULT_TARGET_COUNT);
    }

    private void appendNearbyExpansionCandidates(
            List<GooglePlaceSearchService.GooglePlaceCandidate> googleCandidates
    ) {
        Optional<GooglePlaceSearchService.GooglePlaceCandidate> anchorCandidate =
                findNearbyExpansionAnchor(googleCandidates);
        if (anchorCandidate.isEmpty()) {
            return;
        }

        List<List<GooglePlaceSearchService.GooglePlaceCandidate>> candidateBuckets =
                searchNearbyExpansionCandidateBuckets(anchorCandidate.get());
        appendRoundRobinGoogleCandidates(googleCandidates, candidateBuckets, SEARCH_RESULT_TARGET_COUNT);
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
            GooglePlaceSearchService.GooglePlaceCandidate anchorCandidate
    ) {
        List<List<GooglePlaceSearchService.GooglePlaceCandidate>> candidateBuckets = new ArrayList<>();

        for (List<String> includedTypes : NEARBY_EXPANSION_INCLUDED_TYPE_GROUPS) {
            List<GooglePlaceSearchService.GooglePlaceCandidate> nearbyCandidates =
                    googlePlaceSearchService.searchNearbyPlaces(
                            anchorCandidate.latitude(),
                            anchorCandidate.longitude(),
                            NEARBY_EXPANSION_RADIUS_METERS,
                            includedTypes,
                            NEARBY_EXPANSION_RESULT_COUNT_PER_GROUP,
                            GOOGLE_NEARBY_POPULARITY_RANK_PREFERENCE
                    );

            if (!isNullOrEmpty(nearbyCandidates)) {
                candidateBuckets.add(nearbyCandidates);
            }
        }

        return candidateBuckets;
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
     * Google Places 단건 결과를 place 엔티티 한 건으로 upsert 합니다.
     * 이미지와 소개글이 비어 있으면 Details API/Photo API 결과를 우선 사용해 채웁니다.
     *
     * @param candidate Google Places 후보 1건
     * @return 저장 또는 갱신된 place 엔티티
     */
    private Place syncSingleGooglePlace(GooglePlaceSearchService.GooglePlaceCandidate candidate) {
        if (candidate == null || !hasText(candidate.googlePlaceId()) || !hasText(candidate.name())) {
            return null;
        }

        GooglePlaceSearchService.GooglePlaceCandidate enrichedCandidate = enrichCandidateWithDetails(candidate);

        String name = truncate(enrichedCandidate.name().trim(), PLACE_NAME_MAX_LENGTH);
        String address = truncate(nullableTrim(enrichedCandidate.formattedAddress()), ADDRESS_MAX_LENGTH);

        // 주소 문자열만 믿지 않고 Google address components를 우선 활용해 국가/도시를 파싱합니다.
        GoogleAddressParser.ParsedAddress parsedAddress = GoogleAddressParser.parse(
                address,
                enrichedCandidate.addressComponents()
        );
        String countryName = truncate(nullableTrim(parsedAddress.countryName()), COUNTRY_NAME_MAX_LENGTH);
        if (!hasText(countryName)) {
            countryName = DEFAULT_COUNTRY_NAME;
        }
        String cityName = truncate(nullableTrim(parsedAddress.cityName()), CITY_NAME_MAX_LENGTH);

        BigDecimal latitude = normalizeCoordinate(enrichedCandidate.latitude());
        BigDecimal longitude = normalizeCoordinate(enrichedCandidate.longitude());

        Place place = findExistingPlaceForGoogleSync(
                enrichedCandidate.googlePlaceId(),
                name,
                address,
                cityName,
                countryName
        ).orElse(null);
        PlaceType placeType = resolvePlaceTypeForSync(enrichedCandidate, place);

        String description = resolveDescriptionForSync(place, enrichedCandidate.editorialSummary());
        String openingHours = resolveOpeningHoursForSync(place, enrichedCandidate.regularOpeningWeekdayDescriptions());
        String imageUrl = resolveImageUrlForSync(place, enrichedCandidate.firstPhotoName());
        BigDecimal googleRatingAvg = normalizeGoogleRating(enrichedCandidate.rating(), enrichedCandidate.userRatingCount());
        Integer googleReviewCount = normalizeGoogleReviewCount(enrichedCandidate.userRatingCount());

        if (place == null) {
            return placeRepository.save(Place.builder()
                    .name(name)
                    .googlePlaceId(enrichedCandidate.googlePlaceId())
                    .countryName(countryName)
                    .cityName(cityName)
                    .address(address)
                    .description(description)
                    .latitude(latitude)
                    .longitude(longitude)
                    .placeType(placeType)
                    .openingHours(openingHours)
                    .imageUrl(imageUrl)
                    .googleRatingAvg(googleRatingAvg)
                    .googleReviewCount(googleReviewCount)
                    .build());
        }

        place.updateFromGoogle(
                enrichedCandidate.googlePlaceId(),
                name,
                countryName,
                cityName,
                address,
                latitude,
                longitude,
                placeType,
                description,
                openingHours,
                imageUrl,
                googleRatingAvg,
                googleReviewCount
        );
        return place;
    }

    /**
     * Text Search 응답에 메타데이터가 비어 있을 때만 Place Details를 추가 조회해 병합합니다.
     *
     * @param candidate Text Search 결과 1건
     * @return 메타데이터가 보강된 후보 객체
     */
    private GooglePlaceSearchService.GooglePlaceCandidate enrichCandidateWithDetails(
            GooglePlaceSearchService.GooglePlaceCandidate candidate
    ) {
        if (candidate == null || !hasText(candidate.googlePlaceId())) {
            return candidate;
        }

        if (!needsDetailsFallback(candidate)) {
            return candidate;
        }

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
        return !hasText(candidate.editorialSummary())
                || isNullOrEmpty(candidate.regularOpeningWeekdayDescriptions())
                || !hasText(candidate.firstPhotoName())
                || isNullOrEmpty(candidate.addressComponents());
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
     * Google photo 리소스가 있으면 실제 photoUri를 받아오고,
     * 실패하면 기존 place 이미지 값을 유지합니다.
     *
     * @param place 기존 place 엔티티
     * @param photoName Google photo 리소스명
     * @return 저장할 이미지 URL
     */
    private String resolveImageUrlForSync(Place place, String photoName) {
        if (hasText(photoName)) {
            String photoUri = googlePlaceSearchService.getPhotoUri(photoName);
            if (hasText(photoUri)) {
                return truncate(photoUri, IMAGE_URL_MAX_LENGTH);
            }
        }

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
     * Google Place ID가 없거나 불안정한 경우를 대비해 이름+주소로 기존 place를 찾습니다.
     *
     * @param name 장소명
     * @param address 주소
     * @return 일치하는 place가 있으면 Optional
     */
    private Optional<Place> findByNameAndAddress(String name, String address) {
        if (!hasText(name) || !hasText(address)) {
            return Optional.empty();
        }
        return placeRepository.findFirstByNameAndAddressAndIsDeletedFalse(name, address);
    }

    private Optional<Place> findExistingPlaceForGoogleSync(
            String googlePlaceId,
            String name,
            String address,
            String cityName,
            String countryName
    ) {
        if (hasText(googlePlaceId)) {
            Optional<Place> placeByGooglePlaceId = placeRepository.findByGooglePlaceIdAndIsDeletedFalse(googlePlaceId);
            if (placeByGooglePlaceId.isPresent()) {
                return placeByGooglePlaceId;
            }
        }

        Optional<Place> placeByNameAndAddress = findByNameAndAddress(name, address);
        if (placeByNameAndAddress.isPresent()) {
            return placeByNameAndAddress;
        }

        return findByNameAndRegion(name, cityName, countryName);
    }

    private Optional<Place> findByNameAndRegion(String name, String cityName, String countryName) {
        if (!hasText(name) || !hasText(cityName) || !hasText(countryName)) {
            return Optional.empty();
        }

        return placeRepository.findFirstByNameAndCityNameAndCountryNameAndIsDeletedFalse(
                name,
                cityName,
                countryName
        );
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
            case "beach", "park", "national_park", "botanical_garden", "garden", "campground",
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
}
