package mars.tripplanappbackend.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SocialLoginRequestDto {

    @Schema(description = "소셜에서 발급받은 Access Token", example = "5S3U5IIz3XQNPYdn8ZYd-e_eaLiPu_Z6AAAAAQoXAVAAAAGdFBSoFh7SOb8w2j0_")
    @NotBlank(message = "카카오 토큰은 필수입니다.")
    private String accessToken;
}

