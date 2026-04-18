package mars.tripplanappbackend.search.service;

import java.util.Arrays;
import java.util.List;

/**
 * Google Places 주소 데이터(formattedAddress + addressComponents)를 기준으로
 * country/city를 안정적으로 추출하는 전용 파서입니다.
 *
 * <p>파싱 우선순위:
 * <ol>
 *     <li>addressComponents 기반 파싱 (가장 신뢰도 높음)</li>
 *     <li>한국어 주소 전용 fallback 파싱</li>
 *     <li>일반 콤마 기반 fallback 파싱</li>
 * </ol>
 */
final class GoogleAddressParser {

    static final String DEFAULT_COUNTRY_NAME = "UNKNOWN";
    private static final String KOREA_COUNTRY_NAME = "대한민국";

    private static final List<String> KOREA_MAJOR_REGION_SUFFIXES = List.of(
            "특별시", "광역시", "특별자치시", "특별자치도", "자치도", "도"
    );
    private static final List<String> KOREA_MINOR_REGION_SUFFIXES = List.of("시", "군", "구");

    private GoogleAddressParser() {
    }

    static ParsedAddress parse(
            String formattedAddress,
            List<GooglePlaceSearchService.GoogleAddressComponentCandidate> addressComponents
    ) {
        ParsedAddress fromComponents = parseFromAddressComponents(addressComponents);
        if (fromComponents.hasAnyValue()) {
            return fromComponents.withDefaultCountry();
        }

        ParsedAddress fromKoreanRule = parseWithKoreanRule(formattedAddress);
        if (fromKoreanRule.hasAnyValue()) {
            return fromKoreanRule.withDefaultCountry();
        }

        return parseWithGenericRule(formattedAddress).withDefaultCountry();
    }

    private static ParsedAddress parseFromAddressComponents(
            List<GooglePlaceSearchService.GoogleAddressComponentCandidate> components
    ) {
        if (components == null || components.isEmpty()) {
            return ParsedAddress.empty();
        }

        String country = null;
        String locality = null;
        String adminLevel1 = null;
        String adminLevel2 = null;
        String subLocality = null;

        for (GooglePlaceSearchService.GoogleAddressComponentCandidate component : components) {
            if (component == null || component.types() == null || component.types().isEmpty()) {
                continue;
            }

            String value = firstNonBlank(component.longText(), component.shortText());
            if (!hasText(value)) {
                continue;
            }

            if (containsType(component.types(), "country")) {
                country = value;
            }
            if (containsType(component.types(), "locality")) {
                locality = value;
            }
            if (containsType(component.types(), "administrative_area_level_1")) {
                adminLevel1 = value;
            }
            if (containsType(component.types(), "administrative_area_level_2")) {
                adminLevel2 = value;
            }
            if (containsType(component.types(), "sublocality")
                    || containsTypePrefix(component.types(), "sublocality_level_")) {
                subLocality = value;
            }
        }

        String city = firstNonBlank(locality, adminLevel2, adminLevel1, subLocality);
        return ParsedAddress.of(country, city);
    }

    /**
     * 한국형 주소 포맷 fallback.
     * 예) "대한민국 제주특별자치도 제주시 ...", "서울특별시 강남구 ..."
     */
    private static ParsedAddress parseWithKoreanRule(String formattedAddress) {
        if (!hasText(formattedAddress) || !containsHangul(formattedAddress)) {
            return ParsedAddress.empty();
        }

        String normalizedAddress = formattedAddress.replace(",", " ").trim();
        if (!hasText(normalizedAddress)) {
            return ParsedAddress.empty();
        }

        String[] tokens = normalizedAddress.split("\\s+");
        if (tokens.length == 0) {
            return ParsedAddress.empty();
        }

        String country = null;
        int startIndex = 0;

        if ("대한민국".equals(tokens[0]) || "한국".equals(tokens[0])) {
            country = KOREA_COUNTRY_NAME;
            startIndex = 1;
        } else {
            // 한글 주소는 국가명이 생략되는 경우가 많아 기본값을 대한민국으로 간주합니다.
            country = KOREA_COUNTRY_NAME;
        }

        String city = extractKoreanCity(tokens, startIndex);
        return ParsedAddress.of(country, city);
    }

    private static String extractKoreanCity(String[] tokens, int startIndex) {
        int majorRegionIndex = findTokenIndexBySuffix(tokens, startIndex, KOREA_MAJOR_REGION_SUFFIXES);
        if (majorRegionIndex >= 0) {
            String majorRegion = tokens[majorRegionIndex];

            // 제주특별자치도처럼 도 단위가 먼저 나오면 다음 시/군/구를 city로 우선 채택합니다.
            if (endsWithAny(majorRegion, "도", "특별자치도", "자치도")) {
                int minorRegionIndex = findTokenIndexBySuffix(tokens, majorRegionIndex + 1, KOREA_MINOR_REGION_SUFFIXES);
                if (minorRegionIndex >= 0) {
                    return tokens[minorRegionIndex];
                }
            }
            return majorRegion;
        }

        int cityLikeIndex = findTokenIndexBySuffix(tokens, startIndex, List.of("시"));
        if (cityLikeIndex >= 0) {
            return tokens[cityLikeIndex];
        }

        int districtLikeIndex = findTokenIndexBySuffix(tokens, startIndex, List.of("군", "구"));
        if (districtLikeIndex >= 0) {
            return tokens[districtLikeIndex];
        }

        return null;
    }

    /**
     * 국제 주소 fallback.
     * 예) "1 Chome ..., Osaka, Japan" -> city=Osaka, country=Japan
     */
    private static ParsedAddress parseWithGenericRule(String formattedAddress) {
        if (!hasText(formattedAddress) || !formattedAddress.contains(",")) {
            return ParsedAddress.empty();
        }

        List<String> tokens = Arrays.stream(formattedAddress.split(","))
                .map(String::trim)
                .filter(GoogleAddressParser::hasText)
                .toList();

        if (tokens.isEmpty()) {
            return ParsedAddress.empty();
        }

        String country = tokens.get(tokens.size() - 1);
        String city = tokens.size() >= 2 ? tokens.get(tokens.size() - 2) : null;
        return ParsedAddress.of(country, city);
    }

    private static int findTokenIndexBySuffix(String[] tokens, int fromIndex, List<String> suffixes) {
        for (int index = fromIndex; index < tokens.length; index++) {
            if (endsWithAny(tokens[index], suffixes.toArray(String[]::new))) {
                return index;
            }
        }
        return -1;
    }

    private static boolean containsType(List<String> types, String targetType) {
        if (types == null || !hasText(targetType)) {
            return false;
        }
        return types.stream().anyMatch(targetType::equalsIgnoreCase);
    }

    private static boolean containsTypePrefix(List<String> types, String prefix) {
        if (types == null || !hasText(prefix)) {
            return false;
        }
        return types.stream().anyMatch(type -> type != null && type.toLowerCase().startsWith(prefix.toLowerCase()));
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static boolean containsHangul(String value) {
        if (value == null) {
            return false;
        }
        for (char ch : value.toCharArray()) {
            if (ch >= '\uAC00' && ch <= '\uD7A3') {
                return true;
            }
        }
        return false;
    }

    private static boolean endsWithAny(String value, String... suffixes) {
        if (!hasText(value) || suffixes == null) {
            return false;
        }
        for (String suffix : suffixes) {
            if (hasText(suffix) && value.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    record ParsedAddress(String countryName, String cityName) {
        static ParsedAddress empty() {
            return new ParsedAddress(null, null);
        }

        static ParsedAddress of(String countryName, String cityName) {
            return new ParsedAddress(trimToNull(countryName), trimToNull(cityName));
        }

        boolean hasAnyValue() {
            return hasText(countryName) || hasText(cityName);
        }

        ParsedAddress withDefaultCountry() {
            if (hasText(countryName)) {
                return this;
            }
            return new ParsedAddress(DEFAULT_COUNTRY_NAME, cityName);
        }

        private static String trimToNull(String value) {
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
    }
}
