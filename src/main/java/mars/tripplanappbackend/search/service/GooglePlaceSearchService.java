package mars.tripplanappbackend.search.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

/**
 * Google Places Text Search(Open API)를 호출해 지도 검색 후보를 조회합니다.
 * API 키는 .env -> application.yml 경로로 주입받아 사용합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GooglePlaceSearchService {

    private static final String GOOGLE_PLACES_TEXT_SEARCH_PATH = "/v1/places:searchText";
    private static final String GOOGLE_PLACES_FIELD_MASK =
            "places.id,places.displayName,places.formattedAddress,places.location,places.rating";
    private static final int DEFAULT_MAX_RESULT_COUNT = 20;
    private static final String DEFAULT_LANGUAGE_CODE = "ko";

    @Value("${google.places.api-key:}")
    private String googlePlacesApiKey;

    @Value("${google.places.base-url:https://places.googleapis.com}")
    private String googlePlacesBaseUrl;

    private final WebClient webClient = WebClient.builder().build();

    /**
     * Google Places Text Search API를 호출해 지도 검색 후보를 반환합니다.
     *
     * @param keyword 지도 검색 키워드
     * @return Google Places 검색 후보 목록
     */
    public List<GooglePlaceCandidate> searchPlaces(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        validateGooglePlacesApiKey();

        try {
            GoogleTextSearchResponse response = webClient.post()
                    .uri(googlePlacesBaseUrl + GOOGLE_PLACES_TEXT_SEARCH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Goog-Api-Key", googlePlacesApiKey)
                    .header("X-Goog-FieldMask", GOOGLE_PLACES_FIELD_MASK)
                    .bodyValue(new GoogleTextSearchRequest(
                            normalizedKeyword,
                            DEFAULT_MAX_RESULT_COUNT,
                            DEFAULT_LANGUAGE_CODE
                    ))
                    .retrieve()
                    .bodyToMono(GoogleTextSearchResponse.class)
                    .block();

            return mapSearchResults(response);
        } catch (WebClientResponseException exception) {
            log.error(
                    "Google Places 검색 API 호출 실패. status={}, response={}",
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString()
            );
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        } catch (Exception exception) {
            log.error("Google Places 검색 처리 중 예외 발생: {}", exception.getMessage(), exception);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return keyword.trim();
    }

    private void validateGooglePlacesApiKey() {
        if (googlePlacesApiKey == null
                || googlePlacesApiKey.isBlank()
                || "REPLACE_WITH_YOUR_GOOGLE_PLACES_API_KEY".equals(googlePlacesApiKey.trim())) {
            log.error("GOOGLE_PLACES_API_KEY 값이 비어 있거나 placeholder 상태입니다.");
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private List<GooglePlaceCandidate> mapSearchResults(GoogleTextSearchResponse response) {
        if (response == null || response.getPlaces() == null || response.getPlaces().isEmpty()) {
            return List.of();
        }

        return response.getPlaces().stream()
                .map(place -> new GooglePlaceCandidate(
                        place.getId(),
                        place.getDisplayName() != null ? place.getDisplayName().getText() : null,
                        place.getFormattedAddress(),
                        place.getLocation() != null ? place.getLocation().getLatitude() : null,
                        place.getLocation() != null ? place.getLocation().getLongitude() : null,
                        place.getRating()
                ))
                .toList();
    }

    public record GooglePlaceCandidate(
            String googlePlaceId,
            String name,
            String formattedAddress,
            Double latitude,
            Double longitude,
            Double rating
    ) {
    }

    private record GoogleTextSearchRequest(String textQuery, int maxResultCount, String languageCode) {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GoogleTextSearchResponse {
        private List<GooglePlacePayload> places;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GooglePlacePayload {
        private String id;
        private GoogleDisplayName displayName;
        private String formattedAddress;
        private GoogleLocation location;
        private Double rating;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GoogleDisplayName {
        private String text;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GoogleLocation {
        private Double latitude;
        private Double longitude;
    }
}
