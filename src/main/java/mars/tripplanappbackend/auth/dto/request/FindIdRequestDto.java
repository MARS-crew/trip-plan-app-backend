package mars.tripplanappbackend.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FindIdRequestDto {

    @Schema(description = "닉네임", example = "dmdkr")
    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;

    @Schema(description = "이메일", example = "cye4526@naver.com")
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;
}

