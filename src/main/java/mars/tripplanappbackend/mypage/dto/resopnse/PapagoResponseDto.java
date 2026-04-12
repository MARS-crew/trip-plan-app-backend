package mars.tripplanappbackend.mypage.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PapagoResponseDto {
    @Schema(description = "원문", example = "안녕하세요")
    private String originalText;

    @Schema(description = "번역된 문장", example = "Hello")
    private String translatedText;

    @Schema(description = "번역된 언어", example = "en")
    private String targetLang;

    @Schema(description = "한국어 발음", example = "아리가토우")
    private String pronounce;
}
