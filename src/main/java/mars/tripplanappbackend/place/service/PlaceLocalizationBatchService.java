package mars.tripplanappbackend.place.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mars.tripplanappbackend.global.service.LocationNameLocalizationService;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.repository.PlaceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceLocalizationBatchService {

    private static final int DEFAULT_BATCH_SIZE = 200;

    private final PlaceRepository placeRepository;
    private final LocationNameLocalizationService locationNameLocalizationService;
    private final EntityManager entityManager;

    @Transactional
    public BatchResult localizeAllPlacesToKorean(int batchSize) {
        int normalizedBatchSize = batchSize > 0 ? batchSize : DEFAULT_BATCH_SIZE;
        long scannedCount = 0L;
        long updatedCount = 0L;
        int pageNumber = 0;

        while (true) {
            Page<Place> page = placeRepository.findAllByIsDeletedFalse(
                    PageRequest.of(pageNumber, normalizedBatchSize, Sort.by(Sort.Direction.ASC, "placeId"))
            );

            if (page.isEmpty()) {
                break;
            }

            scannedCount += page.getNumberOfElements();

            for (Place place : page.getContent()) {
                if (localizePlace(place)) {
                    updatedCount++;
                }
            }

            placeRepository.flush();
            entityManager.clear();

            log.info(
                    "Place localization batch progress. page={}, pageSize={}, scanned={}, updated={}",
                    pageNumber,
                    page.getNumberOfElements(),
                    scannedCount,
                    updatedCount
            );

            if (!page.hasNext()) {
                break;
            }

            pageNumber++;
        }

        return new BatchResult(scannedCount, updatedCount);
    }

    private boolean localizePlace(Place place) {
        String originalCountryName = place.getCountryName();
        String originalCityName = place.getCityName();

        String localizedCountryName =
                locationNameLocalizationService.localizeCountryNameToKorean(originalCountryName);
        String localizedCityName =
                locationNameLocalizationService.localizeCityNameToKorean(originalCityName);

        boolean countryChanged = hasChanged(originalCountryName, localizedCountryName);
        boolean cityChanged = hasChanged(originalCityName, localizedCityName);

        if (!countryChanged && !cityChanged) {
            return false;
        }

        place.backfillGoogleMetadata(
                null,
                null,
                localizedCountryName,
                localizedCityName,
                null,
                null,
                null,
                null,
                null,
                null
        );

        return true;
    }

    private boolean hasChanged(String originalValue, String localizedValue) {
        String normalizedOriginal = trimToNull(originalValue);
        String normalizedLocalized = trimToNull(localizedValue);

        if (normalizedOriginal == null && normalizedLocalized == null) {
            return false;
        }
        if (normalizedOriginal == null || normalizedLocalized == null) {
            return true;
        }
        return !normalizedOriginal.equals(normalizedLocalized);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record BatchResult(long scannedCount, long updatedCount) {
    }
}
