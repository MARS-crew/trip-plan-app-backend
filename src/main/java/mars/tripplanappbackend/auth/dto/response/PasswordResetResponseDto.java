package mars.tripplanappbackend.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetResponseDto {
    @Schema(description = "로그인 아이디", example = "cye4526")
    @NotNull(message = "필수 입력값입니다.")
    @Size(min=3, max=40)
    private String usersId;

    @Schema(description = "이메일", example = "cye452687@gmail.com")
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;
}
