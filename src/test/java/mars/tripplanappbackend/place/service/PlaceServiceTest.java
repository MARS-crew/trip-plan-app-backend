package mars.tripplanappbackend.place.service;

import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import mars.tripplanappbackend.mypage.repository.SavedPlaceRepository;
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
                        )
                )
        );

        RecommendedPlaceListResponseDto response = placeService.getRecommendedPlaces(new RecommendedPlaceRequestDto());

        assertThat(response.getRecommendedPlaces()).hasSize(1);
        assertThat(response.getRecommendedPlaces().get(0).getImageUrl())
                .isEqualTo("https://lh3.googleusercontent.com/photo-1");
        assertThat(place.getImageUrl()).isEqualTo("https://lh3.googleusercontent.com/photo-1");
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

        private StubGooglePlaceSearchService(
                Map<String, GooglePlaceCandidate> detailsByPlaceId,
                Map<String, String> photoUriByPhotoName
        ) {
            this.detailsByPlaceId = detailsByPlaceId;
            this.photoUriByPhotoName = photoUriByPhotoName;
        }

        @Override
        public GooglePlaceCandidate getPlaceDetails(String googlePlaceId) {
            return detailsByPlaceId.get(googlePlaceId);
        }

        @Override
        public String getPhotoUri(String photoName) {
            return photoUriByPhotoName.get(photoName);
        }
    }
}
