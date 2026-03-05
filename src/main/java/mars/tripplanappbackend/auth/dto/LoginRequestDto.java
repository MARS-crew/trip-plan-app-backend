package mars.tripplanappbackend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequestDto {
    @Schema(description = "로그인 아이디", example = "cye4526")
    @NotNull(message = "필수 입력값입니다.")
    @Size(min=3, max=15)
    private String usersId;

    @Schema(description = "비밀번호", example = "cye1111*")
    @NotNull(message = "비밀번호를 입력해주세요.")
    @Size(min=8, max=20)
    private String password;
}
