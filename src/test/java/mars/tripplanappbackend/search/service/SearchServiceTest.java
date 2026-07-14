package mars.tripplanappbackend.search.service;

import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.enums.PlaceType;
import mars.tripplanappbackend.search.enums.SearchCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SearchServiceTest {

    private final SearchService searchService = new SearchService(
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
    }

    @Test
    @DisplayName("검색 동기화 대상 개수는 현재 페이지에 필요한 만큼만 계산해 Google quota 사용을 줄인다")
    void calculateRequiredResultCountUsesCurrentPageOnly() throws Exception {
        assertThat(invokeCalculateRequiredResultCount(0, 20)).isEqualTo(20);
        assertThat(invokeCalculateRequiredResultCount(1, 20)).isEqualTo(40);
    }

    @Test
    @DisplayName("Google search errors are treated as empty search candidates")
    void googleSearchErrorsReturnEmptyCandidates() throws Exception {
        SearchService resilientSearchService = new SearchService(
                null,
                null,
                null,
                null,
                null,
                null,
                new ThrowingGooglePlaceSearchService()
        );

        List<GooglePlaceSearchService.GooglePlaceCandidate> candidates =
                invokeCollectGoogleSearchCandidates(resilientSearchService, "no result keyword", 20);

        assertThat(candidates).isEmpty();
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

        @Override
        public List<GooglePlaceCandidate> searchPlaces(String keyword, int resultCount) {
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
    }
}
