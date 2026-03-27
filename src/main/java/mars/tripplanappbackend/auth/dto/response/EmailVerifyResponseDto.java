package mars.tripplanappbackend.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.global.enums.UseYnEnum;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerifyResponseDto {

    @Schema(description = "이메일", example = "cye452687@gmail.com")
    private String email;

    @Schema(description = "인증 여부", example = "Y (or N)")
    private UseYnEnum email_verified;
}
