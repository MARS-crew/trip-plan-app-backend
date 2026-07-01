package mars.tripplanappbackend.place.service;

import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import mars.tripplanappbackend.mypage.repository.SavedPlaceRepository;
import mars.tripplanappbackend.global.service.LocationNameLocalizationService;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.dto.request.RecommendedPlaceRequestDto;
import mars.tripplanappbackend.place.dto.response.RecommendedPlaceListResponseDto;
import mars.tripplanappbackend.place.enums.PlaceType;
import mars.tripplanappbackend.place.repository.PlaceRepository;
import mars.tripplanappbackend.place.repository.PlaceTagMapRepository;
import mars.tripplanappbackend.review.repository.ReviewImageRepository;
import mars.tripplanappbackend.review.repository.ReviewRepository;
import mars.tripplanappbackend.search.service.GooglePlaceSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceServiceTest {

    @Test
    @DisplayName("recommended places replace placeholder image with google photo url")
    void getRecommendedPlacesReplacesPlaceholderImageWithGooglePhotoUrl() {
        Place place = Place.builder()
                .placeId(2421L)
                .name("QA_REC_01_NO_TAG_OSAKA_CASTLE")
                .googlePlaceId("google-place-2421")
                .countryName("Japan")
                .cityName("Osaka")
                .imageUrl("https://placehold.co/600x400/png?text=QA_REC_01")
                .placeType(PlaceType.LANDMARK)
                .ratingAvg(new BigDecimal("5.0"))
                .reviewCount(1004)
                .build();

        PlaceService placeService = new PlaceService(
                createProxy(MyPageRepository.class, Map.of()),
                createProxy(SavedPlaceRepository.class, Map.of()),
                createProxy(
                        PlaceRepository.class,
                        Map.of("findByIsDeletedFalseOrderByRatingAvgDescReviewCountDesc", List.of(place))
                ),
                createProxy(PlaceTagMapRepository.class, Map.of("findAllByPlace_PlaceIdInAndIsDeletedFalse", List.of())),
                createProxy(ReviewRepository.class, Map.of()),
                createProxy(ReviewImageRepository.class, Map.of()),
                new StubGooglePlaceSearchService(
                        Map.of(
                                "google-place-2421",
                                new GooglePlaceSearchService.GooglePlaceCandidate(
                                        "google-place-2421",
                                        "Osaka Castle",
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        List.of(),
                                        null,
                                        List.of(),
                                        "places/google-place-2421/photos/photo-1",
                                        null,
                                        List.of()
                                )
                        ),
                        Map.of(
                                "places/google-place-2421/photos/photo-1",
                                "https://lh3.googleusercontent.com/photo-1"
                        ),
                        Map.of()
                ),
                new StubLocationNameLocalizationService()
        );

        RecommendedPlaceListResponseDto response = placeService.getRecommendedPlaces(new RecommendedPlaceRequestDto());

        assertThat(response.getRecommendedPlaces()).hasSize(1);
        assertThat(response.getRecommendedPlaces().get(0).getImageUrl())
                .isEqualTo("https://lh3.googleusercontent.com/photo-1");
        assertThat(place.getImageUrl()).isEqualTo("https://lh3.googleusercontent.com/photo-1");
    }

    @Test
    @DisplayName("recommended places repair legacy custom place metadata through google search")
    void getRecommendedPlacesRepairsLegacyCustomPlaceMetadataFromGoogleSearch() {
        Place legacyPlace = Place.builder()
                .placeId(745L)
                .name("teamLab Borderless")
                .countryName("UNKNOWN")
                .address("Azabudai Hills, Tokyo")
                .placeType(PlaceType.LANDMARK)
                .ratingAvg(new BigDecimal("4.0"))
                .reviewCount(1)
                .build();

        GooglePlaceSearchService.GooglePlaceCandidate searchCandidate =
                new GooglePlaceSearchService.GooglePlaceCandidate(
                        "google-place-745",
                        "teamLab Borderless: MORI Building DIGITAL ART MUSEUM",
                        "1-2 Toranomon, Minato City, Tokyo, Japan",
                        null,
                        35.6654,
                        139.7494,
                        null,
                        null,
                        List.of(
                                new GooglePlaceSearchService.GoogleAddressComponentCandidate(
                                        "Japan",
                                        "JP",
                                        List.of("country")
                                ),
                                new GooglePlaceSearchService.GoogleAddressComponentCandidate(
                                        "Minato City",
                                        null,
                                        List.of("locality")
                                )
                        ),
                        "Immersive digital art museum in central Tokyo.",
                        List.of("Mon: 10:00 - 19:00"),
                        "places/google-place-745/photos/photo-1",
                        null,
                        List.of()
                );

        PlaceService placeService = new PlaceService(
                createProxy(MyPageRepository.class, Map.of()),
                createProxy(SavedPlaceRepository.class, Map.of()),
                createProxy(
                        PlaceRepository.class,
                        Map.of("findByIsDeletedFalseOrderByRatingAvgDescReviewCountDesc", List.of(legacyPlace))
                ),
                createProxy(PlaceTagMapRepository.class, Map.of("findAllByPlace_PlaceIdInAndIsDeletedFalse", List.of())),
                createProxy(ReviewRepository.class, Map.of()),
                createProxy(ReviewImageRepository.class, Map.of()),
                new StubGooglePlaceSearchService(
                        Map.of(),
                        Map.of(
                                "places/google-place-745/photos/photo-1",
                                "https://lh3.googleusercontent.com/teamlab-photo"
                        ),
                        Map.of(
                                "teamLab Borderless Azabudai Hills, Tokyo",
                                List.of(searchCandidate)
                        )
                ),
                new StubLocationNameLocalizationService()
        );

        RecommendedPlaceListResponseDto response = placeService.getRecommendedPlaces(new RecommendedPlaceRequestDto());

        assertThat(response.getRecommendedPlaces()).hasSize(1);
        assertThat(response.getRecommendedPlaces().get(0).getCountryName()).isEqualTo("일본");
        assertThat(response.getRecommendedPlaces().get(0).getCityName()).isEqualTo("미나토구");
        assertThat(response.getRecommendedPlaces().get(0).getImageUrl())
                .isEqualTo("https://lh3.googleusercontent.com/teamlab-photo");

        assertThat(legacyPlace.getGooglePlaceId()).isEqualTo("google-place-745");
        assertThat(legacyPlace.getCountryName()).isEqualTo("일본");
        assertThat(legacyPlace.getCityName()).isEqualTo("미나토구");
        assertThat(legacyPlace.getDescription()).isEqualTo("Immersive digital art museum in central Tokyo.");
    }

    @SuppressWarnings("unchecked")
    private static <T> T createProxy(Class<T> type, Map<String, Object> stubbedResults) {
        InvocationHandler handler = new RepositoryInvocationHandler(type, stubbedResults);
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static final class RepositoryInvocationHandler implements InvocationHandler {

        private final Class<?> type;
        private final Map<String, Object> stubbedResults;

        private RepositoryInvocationHandler(Class<?> type, Map<String, Object> stubbedResults) {
            this.type = type;
            this.stubbedResults = stubbedResults;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getDeclaringClass() == Object.class) {
                return handleObjectMethod(proxy, method, args);
            }

            if (stubbedResults.containsKey(method.getName())) {
                return stubbedResults.get(method.getName());
            }

            Class<?> returnType = method.getReturnType();
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == long.class) {
                return 0L;
            }
            if (returnType == int.class) {
                return 0;
            }
            if (returnType == Optional.class) {
                return Optional.empty();
            }
            if (List.class.isAssignableFrom(returnType)) {
                return List.of();
            }
            return null;
        }

        private Object handleObjectMethod(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "toString" -> type.getSimpleName() + "Proxy";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> null;
            };
        }
    }

    private static final class StubGooglePlaceSearchService extends GooglePlaceSearchService {

        private final Map<String, GooglePlaceCandidate> detailsByPlaceId;
        private final Map<String, String> photoUriByPhotoName;
        private final Map<String, List<GooglePlaceCandidate>> searchResultsByQuery;

        private StubGooglePlaceSearchService(
                Map<String, GooglePlaceCandidate> detailsByPlaceId,
                Map<String, String> photoUriByPhotoName,
                Map<String, List<GooglePlaceCandidate>> searchResultsByQuery
        ) {
            this.detailsByPlaceId = detailsByPlaceId;
            this.photoUriByPhotoName = photoUriByPhotoName;
            this.searchResultsByQuery = searchResultsByQuery;
        }

        @Override
        public GooglePlaceCandidate getPlaceDetails(String googlePlaceId) {
            return detailsByPlaceId.get(googlePlaceId);
        }

        @Override
        public String getPhotoUri(String photoName) {
            return photoUriByPhotoName.get(photoName);
        }

        @Override
        public List<GooglePlaceCandidate> searchPlaces(String keyword, int resultCount) {
            return searchResultsByQuery.getOrDefault(keyword, List.of());
        }
    }

    private static final class StubLocationNameLocalizationService extends LocationNameLocalizationService {

        @Override
        public String localizeCountryNameToKorean(String countryName) {
            if ("Japan".equals(countryName)) {
                return "일본";
            }
            return countryName;
        }

        @Override
        public String localizeCityNameToKorean(String cityName) {
            if ("Osaka".equals(cityName)) {
                return "오사카";
            }
            if ("Minato City".equals(cityName)) {
                return "미나토구";
            }
            if ("札幌市".equals(cityName)) {
                return "삿포로";
            }
            return cityName;
        }

        @Override
        public boolean requiresKoreanLocalization(String value) {
            if (value == null || value.isBlank()) {
                return false;
            }
            return !value.matches(".*[가-힣].*");
        }
    }
}
