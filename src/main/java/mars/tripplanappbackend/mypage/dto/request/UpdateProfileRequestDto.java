package mars.tripplanappbackend.mypage.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.mypage.enums.Gender;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDto {

    @Schema(description = "닉네임", example = "dmdkr")
    @Size(max = 20)
    private String nickname;

    @Schema(description = "비밀번호", example = "cye1111*")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
            message = "비밀번호는 8~20자이며, 영문, 숫자, 특수문자를 포함해야 합니다.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Schema(description = "비밀번호 확인", example = "cye1111*")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordConfirm;

    @Schema(description = "성별", example = "FEMALE")
    private Gender gender;

    @Schema(description = "생년월일", example = "2005-07-11")
    private LocalDate birth;

    @Schema(description = "국가", example = "대한민국 / 서울")
    private String countryCode;
}
