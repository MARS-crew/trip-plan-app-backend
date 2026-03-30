package mars.tripplanappbackend.mypage.dto.resopnse;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.enums.Gender;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UpdateProfileResponseDto {

    @Schema(description = "닉네임", example = "dmdkr")
    @Size(max = 20)
    private String nickname;

    @Schema(description = "성별", example = "FEMALE")
    private Gender gender;

    @Schema(description = "생년월일", example = "2005-07-11")
    private LocalDate birth;

    @Schema(description = "국가", example = "대한민국 / 서울")
    private String countryCode;

    public UpdateProfileResponseDto(User user) {
        this.nickname = user.getNickname();
        this.gender = user.getGender();
        this.birth = user.getBirth();
        this.countryCode = user.getCountryCode();
    }
}
