package mars.tripplanappbackend.mypage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PapagoRequestDto {
    @Schema(description = "번역할 언어", example = "en")
    private String targetLang;
}
