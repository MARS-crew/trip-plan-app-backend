package mars.tripplanappbackend.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import mars.tripplanappbackend.global.enums.Role;
import mars.tripplanappbackend.global.enums.UseYnEnum;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.enums.Gender;
import mars.tripplanappbackend.mypage.enums.LoginType;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static mars.tripplanappbackend.global.enums.UseYnEnum.N;

@Getter
@Builder
@JsonPropertyOrder({
        "id",
        "usersId",
        "name",
        "email",
        "role",
        "loginType",
        "nickname",
        "gender",
        "birth",
        "privacyAgreed",
        "marketingAgreed",
        "nightMarketingAgreed",
        "createdAt"
})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupResponseDto {
    @Schema(description = "pk", example = "1")
    private Long id;

    @Schema(description = "로그인 아이디", example = "cye4526")
    private String usersId;

    @Schema(description = "이름", example = "최예은")
    private String name;

    @Schema(description = "이메일", example = "cye4526@naver.com")
    private String email;

    @Schema(description = "권한", example = "USER")
    private Role role;

    @Schema(description = "로그인 방식", example = "LOCAL")
    private LoginType loginType;

    @Schema(description = "닉네임", example = "dmdkr")
    private String nickname;

    @Schema(description = "성별", example = "FEMALE")
    private Gender gender;

    @Schema(description = "생년월일", example = "2005-07-11")
    private LocalDate birth;

    @Schema(description = "서비스 이용약관 동의", example = "Y")
    @Builder.Default
    private UseYnEnum privacyAgreed = N;

    @Schema(description = "앱 푸시 동의", example = "Y")
    @Builder.Default
    private UseYnEnum marketingAgreed = N;

    @Schema(description = "야간 마케팅 동의", example = "Y")
    @Builder.Default
    private UseYnEnum nightMarketingAgreed = N;

    @Schema(description = "계정 생성 일시", example = "2026-01-10T23:45:00")
    private LocalDateTime createdAt;

    public static SignupResponseDto from(User entity) {
        return SignupResponseDto.builder()
                .id(entity.getUserId())
                .usersId(entity.getUsersId())
                .name(entity.getName())
                .email(entity.getEmail())
                .role(entity.getRole())
                .loginType(entity.getLoginType())
                .nickname(entity.getNickname())
                .gender(entity.getGender())
                .birth(entity.getBirth())
                .privacyAgreed(entity.getPrivacyAgreed())
                .marketingAgreed(entity.getMarketingAgreed())
                .nightMarketingAgreed(entity.getNightMarketingAgreed())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
