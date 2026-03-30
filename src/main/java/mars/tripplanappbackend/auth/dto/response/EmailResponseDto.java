package mars.tripplanappbackend.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponseDto {

    @Schema(description = "이메일", example = "cye452687@gmail.com")
    private String email;
}
