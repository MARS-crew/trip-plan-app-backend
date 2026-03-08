package mars.tripplanappbackend.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenReissueRequestDto {
    @Schema(description = "refresh Token", example = "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3Njk3ODU4ODYsImV4cCI6MTc3MDk5NTQ4Nn0.BV09n0tn_D_HIl50yoo3yUysZavzMFdFIJhM7zlX280")
    private String refreshToken;
}
