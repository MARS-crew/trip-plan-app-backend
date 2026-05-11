package mars.tripplanappbackend.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenRequest {
    @Schema(description = "fcm 토큰", example = "토큰값")
    @NotBlank(message = "토큰을 입력해 주세요.")
    private String token;
}
