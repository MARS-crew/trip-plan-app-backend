package mars.tripplanappbackend.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequestDto {
    @Schema(description = "로그인 아이디", example = "cye4526")
    @NotNull(message = "필수 입력값입니다.")
    @Size(min=3, max=40)
    private String usersId;

    @Schema(description = "비밀번호", example = "cye1111*")
    @NotNull(message = "비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
            message = "비밀번호는 8~20자이며, 영문, 숫자, 특수문자를 포함해야 합니다.")
    @Size(min=8, max=20)
    private String password;
}
