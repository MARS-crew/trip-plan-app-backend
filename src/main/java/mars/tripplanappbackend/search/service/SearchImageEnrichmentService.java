package mars.tripplanappbackend.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.repository.PlaceRepository;
import mars.tripplanappbackend.search.event.SearchImageEnrichmentRequestedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

/**
 * 최초 검색 응답과 분리해 Google Place Photo URL을 비동기로 보강합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SearchImageEnrichmentService {

    private static final int IMAGE_URL_MAX_LENGTH = 500;

    private final PlaceRepository placeRepository;
    private final GooglePlaceSearchService googlePlaceSearchService;

    @Transactional
    public void enrichMissingImages(List<SearchImageEnrichmentRequestedEvent.Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        long startedAtNanos = System.nanoTime();
        int attemptedCount = 0;
        int updatedCount = 0;

        for (SearchImageEnrichmentRequestedEvent.Task task : tasks) {
            if (task == null || task.placeId() == null || !hasText(task.photoName())) {
                continue;
            }

            Place place = placeRepository.findById(task.placeId()).orElse(null);
            if (place == null || Boolean.TRUE.equals(place.getIsDeleted()) || hasText(place.getImageUrl())) {
                continue;
            }

            attemptedCount++;
            try {
                String photoUri = googlePlaceSearchService.getPhotoUri(task.photoName());
                if (hasText(photoUri)) {
                    place.updateImageUrlFromGoogle(truncate(photoUri.trim(), IMAGE_URL_MAX_LENGTH));
                    updatedCount++;
                }
            } catch (RuntimeException exception) {
                log.warn(
                        "Async search image enrichment failed. placeId={}, message={}",
                        task.placeId(),
                        exception.getMessage()
                );
            }
        }

        log.info(
                "Async search image enrichment completed. taskCount={}, attemptedCount={}, updatedCount={}, elapsedMs={}",
                tasks.size(),
                attemptedCount,
                updatedCount,
                Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis()
        );
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
