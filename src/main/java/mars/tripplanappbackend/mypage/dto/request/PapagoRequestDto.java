package mars.tripplanappbackend.mypage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.mypage.enums.TargetLanguage;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PapagoRequestDto {
    @Schema(description = "번역할 언어", example = "en",
            allowableValues = {"en", "ja", "zh-CN", "zh-TW", "vi", "th", "id", "fr", "es", "ru", "de", "it"})
    private TargetLanguage targetLang;
}
