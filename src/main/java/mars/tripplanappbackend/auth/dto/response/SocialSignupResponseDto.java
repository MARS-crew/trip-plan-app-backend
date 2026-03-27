package mars.tripplanappbackend.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.mypage.enums.Gender;
import mars.tripplanappbackend.mypage.enums.LoginType;

import java.time.LocalDate;

/**
 * 신규 회원을 위한 회원가입용 프리셋 데이터 DTO
 * * 소셜에서 제공받은 원본 데이터를 우리 서비스의 도메인 모델(Gender, LoginType 등)에
 * 맞게 변환하여 프론트엔드 회원가입 페이지에 전달한다.
 */

@Getter
@Builder
public class SocialSignupResponseDto {

    @Schema(description = "소셜 제공자 타입", example = "KAKAO")
    private LoginType loginType;

    @Schema(description = "소셜 고유 식별자", example = "4123499881")
    private String socialProviderId;

    @Schema(description = "소셜에서 받은 닉네임", example = "cye4526")
    private String nickname;

    @Schema(description = "소셜에서 받은 이메일", example = "cye4526@naver.com")
    private String email;

    @Schema(description = "소셜에서 받은 이름", example = "최예은")
    private String name;

    @Schema(description = "소셜에서 받은 성별", example = "FEMALE")
    private Gender gender;

    @Schema(description = "소셜 정보로 조합한 생년월일", example = "2005-07-11")
    private LocalDate birth;
}

