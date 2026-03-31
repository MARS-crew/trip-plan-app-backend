package mars.tripplanappbackend.mypage.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.enums.Gender;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SettingResponseDto {
    @Schema(description = "닉네임", example = "dmdkr")
    private String nickname;

    @Schema(description = "이메일", example = "cye452687@gmail.com")
    private String email;

    @Schema(description = "생년월일", example = "2005-07-11")
    private LocalDate birth;

    @Schema(description = "성별", example = "FEMALE")
    private Gender gender;

    @Schema(description = "국가", example = "대한민국 / 서울")
    private String countryCode;

    public SettingResponseDto(User user) {
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.birth = user.getBirth();
        this.gender = user.getGender();
        this.countryCode = user.getCountryCode();
    }
}
