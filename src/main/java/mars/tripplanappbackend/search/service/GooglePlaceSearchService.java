package mars.tripplanappbackend.search.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.List;

/**
 * Google Places(Text Search / Place Details / Place Photos) 연동 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GooglePlaceSearchService {

    private static final String GOOGLE_PLACES_TEXT_SEARCH_PATH = "/v1/places:searchText";
    private static final String GOOGLE_PLACES_NEARBY_SEARCH_PATH = "/v1/places:searchNearby";
    private static final String GOOGLE_PLACES_DETAILS_PATH_TEMPLATE = "/v1/places/%s";
    private static final String GOOGLE_PLACES_PHOTO_MEDIA_PATH_TEMPLATE = "/v1/%s/media";

    /**
     * Text Search 응답 필드 마스크.
     * 최초 검색 응답과 캐시 저장에 필요한 최소 필드만 요청합니다.
     * 소개글과 영업시간 같은 상세 메타데이터는 검색 성공 여부에 영향을 주지 않도록 제외합니다.
     */
    private static final String GOOGLE_PLACES_TEXT_SEARCH_FIELD_MASK =
            "places.id,places.displayName,places.formattedAddress,places.shortFormattedAddress,places.location,"
                    + "places.primaryType,places.types,places.photos";

    /**
     * Place Details 응답 필드 마스크.
     * Text Search 결과 메타데이터가 비어 있을 때 fallback으로 사용합니다.
     */
    private static final String GOOGLE_PLACES_DETAILS_FIELD_MASK =
            "id,displayName,formattedAddress,shortFormattedAddress,location,"
                    + "primaryType,types,"
                    + "addressComponents.longText,addressComponents.shortText,addressComponents.types,"
                    + "editorialSummary,regularOpeningHours.weekdayDescriptions,photos";

    private static final int GOOGLE_PLACES_MAX_RESULT_COUNT = 20;
    private static final int DEFAULT_RESULT_COUNT = 20;
    private static final String DEFAULT_LANGUAGE_CODE = "ko";
    private static final int DEFAULT_PHOTO_MAX_WIDTH_PX = 900;
    private static final String DEFAULT_NEARBY_RANK_PREFERENCE = "DISTANCE";

    @Value("${google.places.api-key:}")
    private String googlePlacesApiKey;

    @Value("${google.places.base-url:https://places.googleapis.com}")
    private String googlePlacesBaseUrl;

    @Value("${google.places.request-timeout-ms:4500}")
    private long googlePlacesRequestTimeoutMillis;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient = WebClient.builder().build();

    /**
     * Google Places Text Search API를 호출해 키워드 검색 결과를 반환합니다.
     *
     * @param keyword 검색 키워드
     * @return Google Places 검색 결과 목록
     */
    public List<GooglePlaceCandidate> searchPlaces(String keyword) {
        return searchPlaces(keyword, DEFAULT_RESULT_COUNT);
    }

    public List<GooglePlaceCandidate> searchPlaces(String keyword, int resultCount) {
        String normalizedKeyword = normalizeKeyword(keyword);
        validateGooglePlacesApiKey();
        int pageSize = normalizeResultCount(resultCount);
        long startedAtNanos = System.nanoTime();
        String outcome = "ERROR";

        try {
            GoogleTextSearchResponse response = executeTextSearchRequest(
                    googlePlacesBaseUrl + GOOGLE_PLACES_TEXT_SEARCH_PATH,
                    new GoogleTextSearchRequest(
                            normalizedKeyword,
                            pageSize,
                            DEFAULT_LANGUAGE_CODE
                    )
            );

            List<GooglePlaceCandidate> results = mapTextSearchResults(response);
            outcome = results.isEmpty() ? "EMPTY" : "SUCCESS";
            return results;
        } catch (WebClientResponseException exception) {
            log.error(
                    "Google Places Text Search API 호출 실패. keyword={}, status={}, response={}",
                    normalizedKeyword,
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString()
            );
            throw googlePlacesUnavailable();
        } catch (Exception exception) {
            log.error(
                    "Google Places Text Search 처리 중 예외 발생. keyword={}, message={}",
                    normalizedKeyword,
                    exception.getMessage(),
                    exception
            );
            throw googlePlacesUnavailable();
        } finally {
            log.info(
                    "Google Places call completed. operation=TEXT_SEARCH, keyword={}, outcome={}, elapsedMs={}",
                    normalizedKeyword,
                    outcome,
                    elapsedMillis(startedAtNanos)
            );
        }
    }

    public List<GooglePlaceCandidate> searchNearbyPlaces(
            double latitude,
            double longitude,
            double radiusMeters,
            List<String> includedTypes,
            int maxResultCount
    ) {
        return searchNearbyPlaces(
                latitude,
                longitude,
                radiusMeters,
                includedTypes,
                maxResultCount,
                DEFAULT_NEARBY_RANK_PREFERENCE
        );
    }

    public List<GooglePlaceCandidate> searchNearbyPlaces(
            double latitude,
            double longitude,
            double radiusMeters,
            List<String> includedTypes,
            int maxResultCount,
            String rankPreference
    ) {
        validateGooglePlacesApiKey();
        int normalizedMaxResultCount = normalizeResultCount(maxResultCount);
        String normalizedRankPreference = normalizeRankPreference(rankPreference);
        long startedAtNanos = System.nanoTime();
        String outcome = "ERROR";

        try {
            GoogleTextSearchResponse response = executeTextSearchRequest(
                    googlePlacesBaseUrl + GOOGLE_PLACES_NEARBY_SEARCH_PATH,
                    new GoogleNearbySearchRequest(
                            includedTypes,
                            normalizedMaxResultCount,
                            DEFAULT_LANGUAGE_CODE,
                            normalizedRankPreference,
                            new GoogleLocationRestriction(
                                    new GoogleCircle(
                                            new GoogleNearbySearchCenter(latitude, longitude),
                                            radiusMeters
                                    )
                            )
                    )
            );

            List<GooglePlaceCandidate> results = mapTextSearchResults(response);
            outcome = results.isEmpty() ? "EMPTY" : "SUCCESS";
            return results;
        } catch (WebClientResponseException exception) {
            log.error(
                    "Google Places Nearby Search API 호출 실패. lat={}, lng={}, status={}, response={}",
                    latitude,
                    longitude,
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString()
            );
            throw googlePlacesUnavailable();
        } catch (Exception exception) {
            log.error(
                    "Google Places Nearby Search 처리 중 예외 발생. lat={}, lng={}, message={}",
                    latitude,
                    longitude,
                    exception.getMessage(),
                    exception
            );
            throw googlePlacesUnavailable();
        } finally {
            log.info(
                    "Google Places call completed. operation=NEARBY_SEARCH, lat={}, lng={}, outcome={}, elapsedMs={}",
                    latitude,
                    longitude,
                    outcome,
                    elapsedMillis(startedAtNanos)
            );
        }
    }

    /**
     * Place ID 기준으로 Place Details를 조회합니다.
     * <p>
     * 본 메서드는 metadata 보강용 fallback API로 사용하고, 조회 실패 시 null을 반환합니다.
     *
     * @param googlePlaceId Google Place ID
     * @return 상세 정보 후보 객체, 실패 시 null
     */
    public GooglePlaceCandidate getPlaceDetails(String googlePlaceId) {
        if (!hasText(googlePlaceId)) {
            return null;
        }
        validateGooglePlacesApiKey();

        String normalizedPlaceId = normalizePlaceId(googlePlaceId);
        String requestPath = String.format(GOOGLE_PLACES_DETAILS_PATH_TEMPLATE, normalizedPlaceId);
        long startedAtNanos = System.nanoTime();
        String outcome = "ERROR";

        try {
            String requestUri = UriComponentsBuilder.fromUriString(googlePlacesBaseUrl + requestPath)
                    .queryParam("languageCode", DEFAULT_LANGUAGE_CODE)
                    .toUriString();

            GooglePlacePayload response = webClient.get()
                    .uri(requestUri)
                    .header("X-Goog-Api-Key", googlePlacesApiKey)
                    .header("X-Goog-FieldMask", GOOGLE_PLACES_DETAILS_FIELD_MASK)
                    .retrieve()
                    .bodyToMono(GooglePlacePayload.class)
                    .timeout(requestTimeout())
                    .block();

            outcome = response == null ? "EMPTY" : "SUCCESS";
            return response != null ? toCandidate(response) : null;
        } catch (WebClientResponseException exception) {
            log.error(
                    "Google Place Details 조회 실패. placeId={}, status={}, response={}",
                    normalizedPlaceId,
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString()
            );
            throw googlePlacesUnavailable();
        } catch (Exception exception) {
            log.error(
                    "Google Place Details 처리 중 예외 발생. placeId={}, message={}",
                    normalizedPlaceId,
                    exception.getMessage(),
                    exception
            );
            throw googlePlacesUnavailable();
        } finally {
            log.info(
                    "Google Places call completed. operation=PLACE_DETAILS, placeId={}, outcome={}, elapsedMs={}",
                    normalizedPlaceId,
                    outcome,
                    elapsedMillis(startedAtNanos)
            );
        }
    }

    /**
     * Place Photo 리소스명을 사용해 photoUri를 조회합니다.
     * <p>
     * 이미지 URL 조회 실패가 검색 전체를 실패시키지 않도록 null로 흡수합니다.
     *
     * @param photoName Place Photos 리소스명(ex. places/{placeId}/photos/{photoResource})
     * @return 이미지 URL(photoUri), 실패 시 null
     */
    public String getPhotoUri(String photoName) {
        if (!hasText(photoName)) {
            return null;
        }
        validateGooglePlacesApiKey();

        String normalizedPhotoName = removeLeadingSlash(photoName.trim());
        String requestPath = String.format(GOOGLE_PLACES_PHOTO_MEDIA_PATH_TEMPLATE, normalizedPhotoName);
        String requestUri = UriComponentsBuilder.fromUriString(googlePlacesBaseUrl + requestPath)
                .queryParam("maxWidthPx", DEFAULT_PHOTO_MAX_WIDTH_PX)
                .queryParam("skipHttpRedirect", true)
                .toUriString();
        long startedAtNanos = System.nanoTime();
        String outcome = "ERROR";

        try {
            GooglePhotoMediaResponse response = webClient.get()
                    .uri(requestUri)
                    .header("X-Goog-Api-Key", googlePlacesApiKey)
                    .retrieve()
                    .bodyToMono(GooglePhotoMediaResponse.class)
                    .timeout(requestTimeout())
                    .block();

            String photoUri = response != null ? nullableTrim(response.getPhotoUri()) : null;
            outcome = hasText(photoUri) ? "SUCCESS" : "EMPTY";
            return photoUri;
        } catch (WebClientResponseException exception) {
            log.warn(
                    "Google Place Photo 조회 실패. photoName={}, status={}, response={}",
                    normalizedPhotoName,
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString()
            );
            return null;
        } catch (Exception exception) {
            log.warn(
                    "Google Place Photo 처리 중 예외 발생. photoName={}, message={}",
                    normalizedPhotoName,
                    exception.getMessage()
            );
            return null;
        } finally {
            log.info(
                    "Google Places call completed. operation=PLACE_PHOTO, photoName={}, outcome={}, elapsedMs={}",
                    normalizedPhotoName,
                    outcome,
                    elapsedMillis(startedAtNanos)
            );
        }
    }

    private GoogleTextSearchResponse executeTextSearchRequest(String requestUri, Object requestBody)
            throws JsonProcessingException {
        String rawResponse = webClient.post()
                .uri(requestUri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Goog-Api-Key", googlePlacesApiKey)
                .header("X-Goog-FieldMask", GOOGLE_PLACES_TEXT_SEARCH_FIELD_MASK)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(requestTimeout())
                .block();

        if (!hasText(rawResponse)) {
            log.warn("Google Places Text Search 응답 본문이 비어 있습니다. uri={}", requestUri);
            return new GoogleTextSearchResponse();
        }

        GoogleTextSearchResponse parsedResponse = objectMapper.readValue(rawResponse, GoogleTextSearchResponse.class);
        if (parsedResponse.getPlaces() == null || parsedResponse.getPlaces().isEmpty()) {
            log.info("Google Places Text Search 결과가 없습니다. uri={}, rawResponse={}", requestUri, rawResponse);
        }
        return parsedResponse;
    }

    private List<GooglePlaceCandidate> mapTextSearchResults(GoogleTextSearchResponse response) {
        if (response == null || response.getPlaces() == null || response.getPlaces().isEmpty()) {
            return List.of();
        }

        return response.getPlaces().stream()
                .map(this::toCandidate)
                .toList();
    }

    private GooglePlaceCandidate toCandidate(GooglePlacePayload place) {
        if (place == null) {
            return null;
        }

        return new GooglePlaceCandidate(
                place.getId(),
                place.getDisplayName() != null ? place.getDisplayName().getText() : null,
                place.getFormattedAddress(),
                place.getShortFormattedAddress(),
                place.getLocation() != null ? place.getLocation().getLatitude() : null,
                place.getLocation() != null ? place.getLocation().getLongitude() : null,
                place.getRating(),
                place.getUserRatingCount(),
                mapAddressComponents(place.getAddressComponents()),
                place.getEditorialSummary() != null ? place.getEditorialSummary().getText() : null,
                place.getRegularOpeningHours() != null
                        && place.getRegularOpeningHours().getWeekdayDescriptions() != null
                        ? place.getRegularOpeningHours().getWeekdayDescriptions()
                        : List.of(),
                extractFirstPhotoName(place.getPhotos()),
                nullableTrim(place.getPrimaryType()),
                place.getTypes() != null ? place.getTypes() : List.of()
        );
    }

    private List<GoogleAddressComponentCandidate> mapAddressComponents(List<GoogleAddressComponentPayload> components) {
        if (components == null || components.isEmpty()) {
            return List.of();
        }

        return components.stream()
                .map(component -> new GoogleAddressComponentCandidate(
                        component.getLongText(),
                        component.getShortText(),
                        component.getTypes() != null ? component.getTypes() : List.of()
                ))
                .toList();
    }

    private String extractFirstPhotoName(List<GooglePhotoPayload> photos) {
        if (photos == null || photos.isEmpty()) {
            return null;
        }

        return photos.stream()
                .map(GooglePhotoPayload::getName)
                .map(this::nullableTrim)
                .filter(this::hasText)
                .findFirst()
                .orElse(null);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return keyword.trim();
    }

    private String normalizePlaceId(String placeId) {
        if (placeId == null) {
            return null;
        }
        String trimmed = placeId.trim();
        if (trimmed.startsWith("places/")) {
            return trimmed.substring("places/".length());
        }
        return trimmed;
    }

    private String removeLeadingSlash(String value) {
        if (value == null) {
            return null;
        }
        return value.startsWith("/") ? value.substring(1) : value;
    }

    private int normalizeResultCount(int resultCount) {
        if (resultCount < 1) {
            return DEFAULT_RESULT_COUNT;
        }
        return Math.min(resultCount, GOOGLE_PLACES_MAX_RESULT_COUNT);
    }

    private String normalizeRankPreference(String rankPreference) {
        if (!hasText(rankPreference)) {
            return DEFAULT_NEARBY_RANK_PREFERENCE;
        }

        String normalizedRankPreference = rankPreference.trim().toUpperCase();
        if ("POPULARITY".equals(normalizedRankPreference) || "DISTANCE".equals(normalizedRankPreference)) {
            return normalizedRankPreference;
        }

        return DEFAULT_NEARBY_RANK_PREFERENCE;
    }

    private String nullableTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private Duration requestTimeout() {
        return Duration.ofMillis(Math.max(googlePlacesRequestTimeoutMillis, 100L));
    }

    private long elapsedMillis(long startedAtNanos) {
        return Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
    }

    private BusinessException googlePlacesUnavailable() {
        return new BusinessException(ErrorCode.GOOGLE_PLACES_UNAVAILABLE);
    }

    private void validateGooglePlacesApiKey() {
        if (googlePlacesApiKey == null
                || googlePlacesApiKey.isBlank()
                || "REPLACE_WITH_YOUR_GOOGLE_PLACES_API_KEY".equals(googlePlacesApiKey.trim())) {
            log.error("GOOGLE_PLACES_API_KEY 값이 비어 있거나 placeholder 상태입니다.");
            throw googlePlacesUnavailable();
        }
    }

    public record GooglePlaceCandidate(
            String googlePlaceId,
            String name,
            String formattedAddress,
            String shortFormattedAddress,
            Double latitude,
            Double longitude,
            Double rating,
            Integer userRatingCount,
            List<GoogleAddressComponentCandidate> addressComponents,
            String editorialSummary,
            List<String> regularOpeningWeekdayDescriptions,
            String firstPhotoName,
            String primaryType,
            List<String> types
    ) {
    }

    public record GoogleAddressComponentCandidate(
            String longText,
            String shortText,
            List<String> types
    ) {
    }

    private record GoogleTextSearchRequest(String textQuery, int pageSize, String languageCode) {
    }

    private record GoogleNearbySearchRequest(
            List<String> includedTypes,
            int maxResultCount,
            String languageCode,
            String rankPreference,
            GoogleLocationRestriction locationRestriction
    ) {
    }

    private record GoogleLocationRestriction(GoogleCircle circle) {
    }

    private record GoogleCircle(GoogleNearbySearchCenter center, double radius) {
    }

    private record GoogleNearbySearchCenter(double latitude, double longitude) {
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
        private String shortFormattedAddress;
        private GoogleLocation location;
        private Double rating;
        private Integer userRatingCount;
        private String primaryType;
        private List<String> types;
        private List<GoogleAddressComponentPayload> addressComponents;
        private GoogleEditorialSummary editorialSummary;
        private GoogleRegularOpeningHours regularOpeningHours;
        private List<GooglePhotoPayload> photos;
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

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GoogleAddressComponentPayload {
        private String longText;
        private String shortText;
        private List<String> types;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GoogleEditorialSummary {
        private String text;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GoogleRegularOpeningHours {
        private List<String> weekdayDescriptions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GooglePhotoPayload {
        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GooglePhotoMediaResponse {
        private String photoUri;
    }
}
