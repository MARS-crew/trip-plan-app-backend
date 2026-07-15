package mars.tripplanappbackend.search.service;

import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.repository.PlaceRepository;
import mars.tripplanappbackend.search.event.SearchImageEnrichmentRequestedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchImageEnrichmentServiceTest {

    @Test
    @DisplayName("검색 응답과 분리된 이미지 보강이 photoUri를 장소에 저장한다")
    void enrichMissingImagesUpdatesPlaceImage() {
        PlaceRepository placeRepository = mock(PlaceRepository.class);
        GooglePlaceSearchService googlePlaceSearchService = mock(GooglePlaceSearchService.class);
        Place place = Place.builder().placeId(1L).name("해운대해수욕장").build();
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));
        when(googlePlaceSearchService.getPhotoUri("places/1/photos/1"))
                .thenReturn("https://example.com/photo.jpg");

        SearchImageEnrichmentService service =
                new SearchImageEnrichmentService(placeRepository, googlePlaceSearchService);

        service.enrichMissingImages(List.of(
                new SearchImageEnrichmentRequestedEvent.Task(1L, "places/1/photos/1")
        ));

        assertThat(place.getImageUrl()).isEqualTo("https://example.com/photo.jpg");
    }

    @Test
    @DisplayName("비동기 이미지 보강 실패는 검색 결과 저장에 영향을 주지 않는다")
    void enrichMissingImagesAbsorbsPhotoFailure() {
        PlaceRepository placeRepository = mock(PlaceRepository.class);
        GooglePlaceSearchService googlePlaceSearchService = mock(GooglePlaceSearchService.class);
        Place place = Place.builder().placeId(1L).name("해운대해수욕장").build();
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));
        when(googlePlaceSearchService.getPhotoUri("places/1/photos/1"))
                .thenThrow(new RuntimeException("photo API unavailable"));

        SearchImageEnrichmentService service =
                new SearchImageEnrichmentService(placeRepository, googlePlaceSearchService);

        assertThatCode(() -> service.enrichMissingImages(List.of(
                new SearchImageEnrichmentRequestedEvent.Task(1L, "places/1/photos/1")
        ))).doesNotThrowAnyException();
        assertThat(place.getImageUrl()).isNull();
    }
}
