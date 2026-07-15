package mars.tripplanappbackend.search.service;

import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.enums.PlaceType;
import mars.tripplanappbackend.place.repository.PlaceRepository;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.search.dto.request.SearchResultListRequestDto;
import mars.tripplanappbackend.search.domain.SearchCache;
import mars.tripplanappbackend.search.enums.SearchCategory;
import mars.tripplanappbackend.search.repository.SearchCachePlaceRepository;
import mars.tripplanappbackend.search.repository.SearchCacheRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchServiceTest {

    private final SearchService searchService = new SearchService(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
    );

    @Test
    @DisplayName("검색 카테고리에 해변을 별도 항목으로 노출한다")
    void searchCategoriesIncludeBeach() {
        assertThat(SearchCategory.BEACH.getCode()).isEqualTo("BEACH");
        assertThat(SearchCategory.BEACH.getName()).isEqualTo("해변");
        assertThat(SearchCategory.BEACH.getSortOrder()).isEqualTo(4);
    }

    @Test
    @DisplayName("해변 검색어는 실제 해변 장소를 찾기 위한 보강 검색어를 함께 사용한다")
    void beachKeywordBuildsBeachFallbackQueries() throws Exception {
        List<String> queries = invokeBuildGoogleSearchQueries("해변");

        assertThat(queries)
                .containsSubsequence(
                        "해변",
                        "해수욕장",
                        "부산 해수욕장",
                        "제주 해변",
                        "강릉 해변",
                        "beach Korea"
                );
    }

    @Test
    @DisplayName("관광지 검색어는 실제 관광명소를 찾기 위한 보강 검색어를 함께 사용한다")
    void touristAttractionKeywordBuildsFallbackQueries() throws Exception {
        List<String> queries = invokeBuildGoogleSearchQueries("관광지");

        assertThat(queries)
                .containsSubsequence(
                        "관광지",
                        "관광명소",
                        "서울 관광명소",
                        "부산 관광명소",
                        "제주 관광지",
                        "tourist attraction Korea"
                );
    }

    @Test
    @DisplayName("맛집 검색어는 실제 음식점을 찾기 위한 보강 검색어를 함께 사용한다")
    void restaurantKeywordBuildsFallbackQueries() throws Exception {
        List<String> queries = invokeBuildGoogleSearchQueries("맛집");

        assertThat(queries)
                .containsSubsequence(
                        "맛집",
                        "음식점",
                        "서울 맛집",
                        "부산 맛집",
                        "제주 맛집",
                        "restaurant Korea"
                );
    }

    @Test
    @DisplayName("카테고리 검색어는 Text Search 실패 시 Nearby Search 타입으로 보강할 수 있다")
    void categoryKeywordsResolveNearbyIncludedTypes() throws Exception {
        assertThat(invokeResolveCategoryNearbyIncludedTypes("맛집"))
                .contains("restaurant", "korean_restaurant", "cafe");
        assertThat(invokeResolveCategoryNearbyIncludedTypes("관광지"))
                .contains("tourist_attraction", "historical_landmark", "museum");
        assertThat(invokeResolveCategoryNearbyIncludedTypes("해변"))
                .containsExactly("beach");
        assertThat(invokeResolveCategoryNearbyIncludedTypes("숙소"))
                .contains("hotel", "resort_hotel", "lodging");
        assertThat(invokeResolveCategoryNearbyIncludedTypes("자연"))
                .contains("park", "national_park", "hiking_area");
        assertThat(invokeResolveCategoryNearbyIncludedTypes("문화"))
                .contains("museum", "art_gallery", "cultural_center");
    }

    @Test
    @DisplayName("일반 키워드는 원문 검색 한 번만 사용하고 카테고리 보강어를 붙이지 않는다")
    void genericKeywordBuildsOnlyOriginalQuery() throws Exception {
        assertThat(invokeBuildGoogleSearchQueries("존재하지않는장소_QA_987654321"))
                .containsExactly("존재하지않는장소_QA_987654321");
    }

    @Test
    @DisplayName("검색 동기화 대상 개수는 현재 페이지에 필요한 만큼만 계산해 Google quota 사용을 줄인다")
    void calculateRequiredResultCountUsesCurrentPageOnly() throws Exception {
        assertThat(invokeCalculateRequiredResultCount(0, 20)).isEqualTo(20);
        assertThat(invokeCalculateRequiredResultCount(1, 20)).isEqualTo(40);
    }

    @Test
    @DisplayName("Empty or small cached results do not trigger repeated Google refresh")
    void emptyOrSmallCachedResultsDoNotTriggerRefresh() throws Exception {
        assertThat(invokeShouldExpandSearchCache(0, 20)).isFalse();
        assertThat(invokeShouldExpandSearchCache(5, 20)).isFalse();
        assertThat(invokeShouldExpandSearchCache(19, 20)).isFalse();
    }

    @Test
    @DisplayName("Full first-page cached results can expand when the next page is requested")
    void fullFirstPageCachedResultsCanExpandForNextPage() throws Exception {
        assertThat(invokeShouldExpandSearchCache(20, 40)).isTrue();
        assertThat(invokeShouldExpandSearchCache(40, 40)).isFalse();
        assertThat(invokeShouldExpandSearchCache(100, 120)).isFalse();
    }

    @Test
    @DisplayName("Google search errors stop fallback searches instead of becoming empty results")
    void googleSearchErrorsStopFallbackSearches() {
        ThrowingGooglePlaceSearchService throwingGooglePlaceSearchService =
                new ThrowingGooglePlaceSearchService();
        SearchService failFastSearchService = new SearchService(
                null,
                null,
                null,
                null,
                null,
                null,
                throwingGooglePlaceSearchService,
                null
        );

        assertThatThrownBy(() ->
                invokeCollectGoogleSearchCandidates(failFastSearchService, "no result keyword", 20))
                .hasRootCauseMessage("text search failed");
        assertThat(throwingGooglePlaceSearchService.getSearchCallCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Google provider errors are not saved as zero-result search caches")
    void googleProviderErrorsAreNotCached() {
        SearchCacheRepository searchCacheRepository = mock(SearchCacheRepository.class);
        GooglePlaceSearchService googlePlaceSearchService = mock(GooglePlaceSearchService.class);
        when(searchCacheRepository.findByKeyword(any())).thenReturn(Optional.empty());
        when(googlePlaceSearchService.searchPlaces(any(), anyInt()))
                .thenThrow(new BusinessException(ErrorCode.GOOGLE_PLACES_UNAVAILABLE));

        SearchService targetSearchService = new SearchService(
                null,
                null,
                null,
                null,
                searchCacheRepository,
                null,
                googlePlaceSearchService,
                null
        );

        SearchResultListRequestDto requestDto =
                SearchResultListRequestDto.of("해변", null, 0, 20);

        assertThatThrownBy(() -> targetSearchService.getSearchResults(requestDto))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GOOGLE_PLACES_UNAVAILABLE);
        verify(searchCacheRepository, never()).save(any());
    }

    @Test
    @DisplayName("정상 Google 응답의 검색 결과 0건은 정상 캐시로 저장한다")
    void normalEmptyGoogleResultsAreCached() {
        SearchCacheRepository searchCacheRepository = mock(SearchCacheRepository.class);
        SearchCachePlaceRepository searchCachePlaceRepository = mock(SearchCachePlaceRepository.class);
        GooglePlaceSearchService googlePlaceSearchService = mock(GooglePlaceSearchService.class);
        when(searchCacheRepository.findByKeyword(any())).thenReturn(Optional.empty());
        when(searchCacheRepository.save(any(SearchCache.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(searchCachePlaceRepository.findAllBySearchCache_SearchCacheIdOrderBySortOrderAsc(any()))
                .thenReturn(List.of());
        when(googlePlaceSearchService.searchPlaces(any(), anyInt())).thenReturn(List.of());

        SearchService targetSearchService = new SearchService(
                null,
                null,
                null,
                null,
                searchCacheRepository,
                searchCachePlaceRepository,
                googlePlaceSearchService,
                null
        );

        SearchResultListRequestDto requestDto =
                SearchResultListRequestDto.of("결과없는검색어", null, 0, 20);

        assertThat(targetSearchService.getSearchResults(requestDto).getTotalCount()).isZero();
        verify(searchCacheRepository).save(any(SearchCache.class));
        verify(googlePlaceSearchService, times(1)).searchPlaces(any(), anyInt());
        verify(googlePlaceSearchService, never()).searchNearbyPlaces(
                anyDouble(),
                anyDouble(),
                anyDouble(),
                any(),
                anyInt(),
                any()
        );
    }

    @Test
    @DisplayName("일반 키워드와 관련 없는 Google 유사 결과는 정상 0건으로 캐시한다")
    void irrelevantFuzzyGoogleResultsAreCachedAsZero() {
        SearchCacheRepository searchCacheRepository = mock(SearchCacheRepository.class);
        SearchCachePlaceRepository searchCachePlaceRepository = mock(SearchCachePlaceRepository.class);
        GooglePlaceSearchService googlePlaceSearchService = mock(GooglePlaceSearchService.class);
        when(searchCacheRepository.findByKeyword(any())).thenReturn(Optional.empty());
        when(searchCacheRepository.save(any(SearchCache.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(searchCachePlaceRepository.findAllBySearchCache_SearchCacheIdOrderBySortOrderAsc(any()))
                .thenReturn(List.of());
        when(googlePlaceSearchService.searchPlaces(any(), anyInt()))
                .thenReturn(List.of(googleCandidate("google-fuzzy-1", "서울 관광 명소")));

        SearchService targetSearchService = new SearchService(
                null, null, null, null,
                searchCacheRepository,
                searchCachePlaceRepository,
                googlePlaceSearchService,
                null
        );

        SearchResultListRequestDto requestDto = SearchResultListRequestDto.of(
                "존재하지않는장소_QA_987654321",
                null,
                0,
                20
        );

        assertThat(targetSearchService.getSearchResults(requestDto).getTotalCount()).isZero();
        verify(searchCacheRepository).save(any(SearchCache.class));
        verify(googlePlaceSearchService, times(1)).searchPlaces(any(), anyInt());
    }

    @Test
    @DisplayName("Google 후보 여러 건은 장소 DB를 후보별 조회하지 않고 일괄 조회·저장한다")
    void googleCandidatesAreLoadedAndSavedInBatch() throws Exception {
        PlaceRepository placeRepository = mock(PlaceRepository.class);
        GooglePlaceSearchService googlePlaceSearchService = mock(GooglePlaceSearchService.class);
        when(googlePlaceSearchService.searchPlaces(any(), anyInt())).thenReturn(List.of(
                googleCandidate("google-batch-1", "테스트장소 A"),
                googleCandidate("google-batch-2", "테스트장소 B")
        ));
        when(placeRepository.findAllByGooglePlaceIdInAndIsDeletedFalseOrderByUpdatedAtDescCreatedAtDescPlaceIdDesc(any()))
                .thenReturn(List.of());
        when(placeRepository.findAllByNameInAndIsDeletedFalse(any())).thenReturn(List.of());
        when(placeRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SearchService targetSearchService = new SearchService(
                null,
                placeRepository,
                null,
                null,
                null,
                null,
                googlePlaceSearchService,
                null
        );

        invokeSyncGooglePlaces(targetSearchService, "테스트장소", 20);

        verify(placeRepository, times(1))
                .findAllByGooglePlaceIdInAndIsDeletedFalseOrderByUpdatedAtDescCreatedAtDescPlaceIdDesc(any());
        verify(placeRepository, times(1)).findAllByNameInAndIsDeletedFalse(any());
        verify(placeRepository, times(1)).saveAll(any());
        verify(placeRepository, never())
                .findAllByGooglePlaceIdAndIsDeletedFalseOrderByUpdatedAtDescCreatedAtDescPlaceIdDesc(any());
    }

    @Test
    @DisplayName("해변 검색 결과에서는 위치 정보가 null로 표시될 장소를 제외한다")
    void beachSearchFiltersPlacesWithMissingLocation() throws Exception {
        Place validBeach = Place.builder()
                .placeId(1L)
                .name("광안리해수욕장")
                .cityName("부산광역시")
                .countryName("대한민국")
                .placeType(PlaceType.BEACH)
                .build();
        Place invalidBeach = Place.builder()
                .placeId(2L)
                .name("해운대해수욕장")
                .cityName("null")
                .countryName("대한민국")
                .placeType(PlaceType.BEACH)
                .build();

        List<Place> filteredPlaces = invokeFilterSearchPlacesForKeyword(
                "해변",
                List.of(validBeach, invalidBeach)
        );

        assertThat(filteredPlaces).containsExactly(validBeach);
    }

    @Test
    @DisplayName("해변이 아닌 검색 결과에서는 위치 정보 필터를 적용하지 않는다")
    void nonBeachSearchDoesNotFilterPlacesWithMissingLocation() throws Exception {
        Place place = Place.builder()
                .placeId(1L)
                .name("해운대 맛집")
                .cityName("null")
                .countryName("대한민국")
                .placeType(PlaceType.RESTAURANT)
                .build();

        List<Place> filteredPlaces = invokeFilterSearchPlacesForKeyword("맛집", List.of(place));

        assertThat(filteredPlaces).containsExactly(place);
    }

    @Test
    @DisplayName("Google Places beach 타입은 앱의 해변 타입으로 저장한다")
    void googleBeachTypeMapsToBeachPlaceType() throws Exception {
        GooglePlaceSearchService.GooglePlaceCandidate candidate =
                new GooglePlaceSearchService.GooglePlaceCandidate(
                        "google-beach-1",
                        "Haeundae Beach",
                        "Busan, Korea",
                        null,
                        35.1587,
                        129.1604,
                        null,
                        null,
                        List.of(),
                        null,
                        List.of(),
                        null,
                        "beach",
                        List.of("beach", "tourist_attraction")
                );

        PlaceType placeType = invokeResolvePlaceTypeFromGoogle(candidate);

        assertThat(placeType).isEqualTo(PlaceType.BEACH);
        assertThat(PlaceType.normalizeForAppCategory(placeType)).isEqualTo(PlaceType.BEACH);
    }

    @SuppressWarnings("unchecked")
    private List<String> invokeBuildGoogleSearchQueries(String keyword) throws Exception {
        Method method = SearchService.class.getDeclaredMethod("buildGoogleSearchQueries", String.class);
        method.setAccessible(true);

        return (List<String>) method.invoke(searchService, keyword);
    }

    private PlaceType invokeResolvePlaceTypeFromGoogle(
            GooglePlaceSearchService.GooglePlaceCandidate candidate
    ) throws Exception {
        Method method = SearchService.class.getDeclaredMethod(
                "resolvePlaceTypeFromGoogle",
                GooglePlaceSearchService.GooglePlaceCandidate.class
        );
        method.setAccessible(true);

        return (PlaceType) method.invoke(searchService, candidate);
    }

    private void invokeSyncGooglePlaces(
            SearchService targetSearchService,
            String keyword,
            int maxCount
    ) throws Exception {
        Method method = SearchService.class.getDeclaredMethod("syncGooglePlaces", String.class, int.class);
        method.setAccessible(true);
        method.invoke(targetSearchService, keyword, maxCount);
    }

    private GooglePlaceSearchService.GooglePlaceCandidate googleCandidate(String googlePlaceId, String name) {
        return new GooglePlaceSearchService.GooglePlaceCandidate(
                googlePlaceId,
                name,
                "서울특별시 대한민국",
                "서울",
                37.5665,
                126.9780,
                4.5,
                100,
                List.of(),
                null,
                List.of(),
                null,
                "tourist_attraction",
                List.of("tourist_attraction")
        );
    }

    @SuppressWarnings("unchecked")
    private List<String> invokeResolveCategoryNearbyIncludedTypes(String keyword) throws Exception {
        Method method = SearchService.class.getDeclaredMethod("resolveCategoryNearbyIncludedTypes", String.class);
        method.setAccessible(true);

        return (List<String>) method.invoke(searchService, keyword);
    }

    private int invokeCalculateRequiredResultCount(int page, int size) throws Exception {
        Method method = SearchService.class.getDeclaredMethod(
                "calculateRequiredResultCount",
                int.class,
                int.class
        );
        method.setAccessible(true);

        return (int) method.invoke(searchService, page, size);
    }

    private boolean invokeShouldExpandSearchCache(int cachedPlaceCount, int requiredResultCount) throws Exception {
        Method method = SearchService.class.getDeclaredMethod(
                "shouldExpandSearchCache",
                int.class,
                int.class
        );
        method.setAccessible(true);

        return (boolean) method.invoke(searchService, cachedPlaceCount, requiredResultCount);
    }

    @SuppressWarnings("unchecked")
    private List<Place> invokeFilterSearchPlacesForKeyword(String keyword, List<Place> places) throws Exception {
        Method method = SearchService.class.getDeclaredMethod(
                "filterSearchPlacesForKeyword",
                String.class,
                List.class
        );
        method.setAccessible(true);

        return (List<Place>) method.invoke(searchService, keyword, places);
    }

    @SuppressWarnings("unchecked")
    private List<GooglePlaceSearchService.GooglePlaceCandidate> invokeCollectGoogleSearchCandidates(
            SearchService targetSearchService,
            String keyword,
            int maxCount
    ) throws Exception {
        Method method = SearchService.class.getDeclaredMethod(
                "collectGoogleSearchCandidates",
                String.class,
                int.class
        );
        method.setAccessible(true);

        return (List<GooglePlaceSearchService.GooglePlaceCandidate>) method.invoke(
                targetSearchService,
                keyword,
                maxCount
        );
    }

    private static class ThrowingGooglePlaceSearchService extends GooglePlaceSearchService {

        private int searchCallCount;

        @Override
        public List<GooglePlaceCandidate> searchPlaces(String keyword, int resultCount) {
            searchCallCount++;
            throw new RuntimeException("text search failed");
        }

        @Override
        public List<GooglePlaceCandidate> searchNearbyPlaces(
                double latitude,
                double longitude,
                double radiusMeters,
                List<String> includedTypes,
                int maxResultCount,
                String rankPreference
        ) {
            throw new RuntimeException("nearby search failed");
        }

        private int getSearchCallCount() {
            return searchCallCount;
        }
    }
}
