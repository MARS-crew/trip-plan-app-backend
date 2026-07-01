package mars.tripplanappbackend.global.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class LocationNameLocalizationService {

    private static final String TRANSLATE_PATH = "/language/translate/v2";
    private static final String TARGET_LANGUAGE_CODE = "ko";
    private static final String UNKNOWN_COUNTRY_NAME = "UNKNOWN";
    private static final Map<String, String> MANUAL_LOCALIZATION_OVERRIDES = Map.of(
            "minato city", "미나토구"
    );

    @Value("${google.translate.api-key:}")
    private String apiKey;

    @Value("${google.translate.base-url:https://translation.googleapis.com}")
    private String baseUrl;

    private final WebClient webClient = WebClient.builder().build();
    private final ConcurrentMap<String, String> localizationCache = new ConcurrentHashMap<>();

    public String localizeCountryNameToKorean(String countryName) {
        if (isUnknownCountry(countryName)) {
            return countryName;
        }
        return localizeToKorean(countryName);
    }

    public String localizeCityNameToKorean(String cityName) {
        return localizeToKorean(cityName);
    }

    public boolean requiresKoreanLocalization(String value) {
        String normalizedValue = trimToNull(value);
        return normalizedValue != null && !containsHangul(normalizedValue);
    }

    protected String localizeToKorean(String value) {
        String normalizedValue = trimToNull(value);
        if (normalizedValue == null || !requiresKoreanLocalization(normalizedValue)) {
            return normalizedValue;
        }

        String overrideValue = resolveManualOverride(normalizedValue);
        if (overrideValue != null) {
            return overrideValue;
        }

        return localizationCache.computeIfAbsent(normalizedValue, this::translateToKoreanOrOriginal);
    }

    private String resolveManualOverride(String value) {
        String normalizedValue = trimToNull(value);
        if (normalizedValue == null) {
            return null;
        }

        return MANUAL_LOCALIZATION_OVERRIDES.get(normalizedValue.toLowerCase(Locale.ROOT));
    }

    private String translateToKoreanOrOriginal(String originalText) {
        if (originalText == null) {
            return null;
        }
        if (apiKey == null || apiKey.isBlank()) {
            return originalText;
        }

        try {
            String uri = UriComponentsBuilder.fromUriString(baseUrl + TRANSLATE_PATH)
                    .queryParam("key", apiKey)
                    .toUriString();

            Map<String, String> body = Map.of(
                    "q", originalText,
                    "target", TARGET_LANGUAGE_CODE,
                    "format", "text"
            );

            Map<?, ?> response = webClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                return originalText;
            }

            Map<?, ?> data = (Map<?, ?>) response.get("data");
            if (data == null) {
                return originalText;
            }

            List<?> translations = (List<?>) data.get("translations");
            if (translations == null || translations.isEmpty()) {
                return originalText;
            }

            Object firstTranslation = translations.get(0);
            if (!(firstTranslation instanceof Map<?, ?> translationMap)) {
                return originalText;
            }

            Object translatedText = translationMap.get("translatedText");
            if (!(translatedText instanceof String translatedValue)) {
                return originalText;
            }

            String normalizedTranslatedValue = trimToNull(HtmlUtils.htmlUnescape(translatedValue));
            return normalizedTranslatedValue != null ? normalizedTranslatedValue : originalText;
        } catch (Exception exception) {
            log.warn("Failed to localize location name to Korean. text={}, message={}", originalText, exception.getMessage());
            return originalText;
        }
    }

    private boolean isUnknownCountry(String value) {
        String normalizedValue = trimToNull(value);
        return normalizedValue != null && UNKNOWN_COUNTRY_NAME.equalsIgnoreCase(normalizedValue);
    }

    private boolean containsHangul(String value) {
        for (char ch : value.toCharArray()) {
            if (ch >= '\uAC00' && ch <= '\uD7A3') {
                return true;
            }
        }
        return false;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
