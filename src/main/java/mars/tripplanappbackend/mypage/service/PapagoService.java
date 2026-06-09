package mars.tripplanappbackend.mypage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mars.tripplanappbackend.mypage.dto.resopnse.PapagoResponseDto;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PapagoService {
    @Value("${google.translate.api-key}")
    private String apiKey;

    @Value("${google.translate.base-url:https://translation.googleapis.com}")
    private String baseUrl;

    private static final String TRANSLATE_PATH = "/language/translate/v2";

    private final WebClient webClient = WebClient.builder().build();

    private static final String SOURCE_LANG = "ko";

    private final MyPageRepository myPageRepository;

    private static final List<String> FIXED_PHRASES = List.of(
            "안녕하세요",
            "이거 얼마예요?",
            "이거 주세요",
            "~로 가려면 어떻게 가야 할까요?",
            "실례합니다"
    );

    /**
     * 5개 고정 문장을 사용자 위치 기반 언어로 번역
     *
     * @param targetLang 번역 대상 언어 코드 (예: "en", "ja", "zh-CN")
     * @return 번역 결과 목록
     * 번역 실패 시 TRANSLATION_FAILED
     */
    public List<PapagoResponseDto> translatePhrases(String usersId, String targetLang) {
        myPageRepository.findByUsersIdAndIsDeletedFalse(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return FIXED_PHRASES.stream()
                .map(text -> translate(text, targetLang))
                .toList();

    }

    /**
     * Google Cloud Translate Basic(v2) API 호출 (내부 전용)
     */
    private PapagoResponseDto translate(String text, String targetLang) {
        try {
            String uri = UriComponentsBuilder.fromUriString(baseUrl + TRANSLATE_PATH)
                    .queryParam("key", apiKey)
                    .toUriString();

            Map<String, String> body = Map.of(
                    "q", text,
                    "source", SOURCE_LANG,
                    "target", targetLang,
                    "format", "text"
            );

            Map response = webClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            List<Map<String, Object>> translations = (List<Map<String, Object>>) data.get("translations");
            String translatedText = (String) translations.get(0).get("translatedText");

            return new PapagoResponseDto(text,
                    translatedText,
                    targetLang,
                    "아리가토우");

        } catch (Exception e) {
            log.error("번역 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TRANSLATION_FAILED);
        }
    }
}
