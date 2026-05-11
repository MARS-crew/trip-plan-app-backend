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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import mars.tripplanappbackend.mypage.dto.resopnse.PapagoResponseDto;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PapagoService {
    @Value("${spring.social.papago.client-key}")
    private String clientId;

    @Value("${spring.social.papago.client-secret}")
    private String clientSecret;

    private final WebClient webClient =
            WebClient.builder()
                    .baseUrl("https://papago.apigw.ntruss.com")
                    .build();

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
     * Papago 번역 API 호출 (내부 전용)
     */
    private PapagoResponseDto translate(String text, String targetLang) {
        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("source", "ko");
            body.add("target", targetLang);
            body.add("text", text);

            Map response = webClient.post()
                    .uri("/nmt/v1/translation")
                    .header("X-NCP-APIGW-API-KEY-ID", clientId)
                    .header("X-NCP-APIGW-API-KEY", clientSecret)
                    .body(BodyInserters.fromFormData(body))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            Map<String, Object> message = (Map<String, Object>) response.get("message");
            Map<String, Object> result  = (Map<String, Object>) message.get("result");
            String translatedText       = (String) result.get("translatedText");

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
