package mars.tripplanappbackend.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 소셜 로그인 API의 최종 응답 DTO
 * * 소셜 토큰 검증 후, 기존 회원이면 login 정보를 담고
 * 신규 회원이면 signupResponse에 소셜 프로필 데이터를 담아 반환한다.
 */

@Getter
@Builder
public class SocialLoginResponseDto {

    @Schema(description = "기존 회원 여부", example = "true")
    private boolean registered;

    @Schema(description = "다음 액션(login/signup)", example = "login")
    private String nextAction;

    @Schema(description = "기존 회원일 때 반환되는 로그인 정보")
    private LoginResponseDto login;

    @Schema(description = "신규 회원일 때 반환되는 회원가입 데이터")
    private SocialSignupResponseDto signupResponse;
}

