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
     * 검색 결과를 place/search_cache에 동기화하는 데 필요한 필드와
     * 화면 표시에 쓰는 메타데이터(이미지/설명/영업시간)를 함께 요청합니다.
     */
    private static final String GOOGLE_PLACES_TEXT_SEARCH_FIELD_MASK =
            "places.id,places.displayName,places.formattedAddress,places.shortFormattedAddress,places.location,"
                    + "places.primaryType,places.types,"
                    + "places.addressComponents.longText,places.addressComponents.shortText,places.addressComponents.types,"
                    + "places.editorialSummary,places.regularOpeningHours.weekdayDescriptions,places.photos";

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

        try {
            GoogleTextSearchResponse response = executeTextSearchRequest(
                    googlePlacesBaseUrl + GOOGLE_PLACES_TEXT_SEARCH_PATH,
                    new GoogleTextSearchRequest(
                            normalizedKeyword,
                            pageSize,
                            DEFAULT_LANGUAGE_CODE
                    )
            );

            return mapTextSearchResults(response);
        } catch (WebClientResponseException exception) {
            if (isTreatableAsEmptyResult(exception)) {
                log.warn(
                        "Google Places Text Search 응답이 비정상이지만 2xx이므로 빈 결과로 처리합니다. keyword={}, status={}, response={}",
                        normalizedKeyword,
                        exception.getStatusCode(),
                        exception.getResponseBodyAsString()
                );
                return List.of();
            }
            log.error(
                    "Google Places Text Search API 호출 실패. keyword={}, status={}, response={}",
                    normalizedKeyword,
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString()
            );
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        } catch (Exception exception) {
            log.error(
                    "Google Places Text Search 처리 중 예외 발생. keyword={}, message={}",
                    normalizedKeyword,
                    exception.getMessage(),
                    exception
            );
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
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

            return mapTextSearchResults(response);
        } catch (WebClientResponseException exception) {
            if (isTreatableAsEmptyResult(exception)) {
                log.warn(
                        "Google Places Nearby Search 응답이 비정상이지만 2xx이므로 빈 결과로 처리합니다. lat={}, lng={}, status={}, response={}",
                        latitude,
                        longitude,
                        exception.getStatusCode(),
                        exception.getResponseBodyAsString()
                );
                return List.of();
            }
            log.warn(
                    "Google Places Nearby Search API 호출 실패. lat={}, lng={}, status={}, response={}",
                    latitude,
                    longitude,
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString()
            );
            return List.of();
        } catch (Exception exception) {
            log.warn(
                    "Google Places Nearby Search 처리 중 예외 발생. lat={}, lng={}, message={}",
                    latitude,
                    longitude,
                    exception.getMessage()
            );
            return List.of();
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
                    .block();

            return response != null ? toCandidate(response) : null;
        } catch (WebClientResponseException exception) {
            log.warn(
                    "Google Place Details 조회 실패. placeId={}, status={}, response={}",
                    normalizedPlaceId,
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString()
            );
            return null;
        } catch (Exception exception) {
            log.warn(
                    "Google Place Details 처리 중 예외 발생. placeId={}, message={}",
                    normalizedPlaceId,
                    exception.getMessage()
            );
            return null;
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

        try {
            GooglePhotoMediaResponse response = webClient.get()
                    .uri(requestUri)
                    .header("X-Goog-Api-Key", googlePlacesApiKey)
                    .retrieve()
                    .bodyToMono(GooglePhotoMediaResponse.class)
                    .block();

            return response != null ? nullableTrim(response.getPhotoUri()) : null;
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

    private boolean isTreatableAsEmptyResult(WebClientResponseException exception) {
        if (exception == null || !exception.getStatusCode().is2xxSuccessful()) {
            return false;
        }

        String responseBody = exception.getResponseBodyAsString();
        if (!hasText(responseBody)) {
            return true;
        }

        String normalizedBody = responseBody.trim();
        return "{}".equals(normalizedBody) || "{\"places\":[]}".equals(normalizedBody);
    }

    private void validateGooglePlacesApiKey() {
        if (googlePlacesApiKey == null
                || googlePlacesApiKey.isBlank()
                || "REPLACE_WITH_YOUR_GOOGLE_PLACES_API_KEY".equals(googlePlacesApiKey.trim())) {
            log.error("GOOGLE_PLACES_API_KEY 값이 비어 있거나 placeholder 상태입니다.");
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
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
