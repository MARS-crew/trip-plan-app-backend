package mars.tripplanappbackend.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import mars.tripplanappbackend.auth.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.global.enums.Gender;
import mars.tripplanappbackend.global.enums.UseYnEnum;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class SignupRequestDto {
    @Schema(description = "로그인 아이디", example = "cye4526")
    @NotNull(message = "필수 입력값입니다.")
    @Size(min=3, max=15)
    private String usersId;

    @Schema(description = "이름", example = "최예은")
    @NotNull(message = "필수 입력값입니다.")
    private String name;

    @Schema(description = "닉네임", example = "dmdkr")
    @NotNull(message = "필수 입력값입니다.")
    private String nickname;

    @Schema(description = "비밀번호", example = "cye1111*")
    @NotNull(message = "필수 입력값입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
            message = "비밀번호는 8~20자이며, 영문, 숫자, 특수문자를 포함해야 합니다.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Schema(description = "비밀번호 확인", example = "cye1111*")
    @NotNull(message = "필수 입력값입니다.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordConfirm;

    @Schema(description = "성별", example = "FEMALE")
    @NotNull(message = "필수 입력값입니다.")
    private Gender gender;

    @Schema(description = "생년월일", example = "2005-07-11")
    @NotNull(message = "필수 입력값입니다.")
    private LocalDate birth;

    // 국가

    @Schema(description = "서비스 이용약관 동의", example = "Y")
    @NotNull(message = "필수 입력값입니다.")
    private UseYnEnum privacyAgreed;

    @Schema(description = "앱 푸시 동의", example = "Y")
    private UseYnEnum marketingAgreed;

    @Schema(description = "야간 마케팅 동의", example = "Y")
    private UseYnEnum nightMarketingAgreed;

    public UserEntity toEntity (String encodedPassword){
        return UserEntity.builder()
                .usersId(this.usersId)
                .name(this.name)
                .nickname(this.nickname)
                .password(encodedPassword)
                .gender(this.gender)
                .birth(this.birth)
                .privacyAgreed(this.privacyAgreed)
                .marketingAgreed(this.marketingAgreed)
                .nightMarketingAgreed(this.nightMarketingAgreed)
                .build();
    }
}
