package mars.tripplanappbackend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenReissueResponseDto {
    @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJrYnMxIiwiaWF0IjoxNzY5Nzg1ODg2LCJleHAiOjE3Njk3ODk0ODZ9.eTAh6bR-deRV-Y_1FMRAhXk0BfsQxMy-E0KkHhBuBDk")
    private String accessToken;

    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJrYnMxIiwiaWF0IjoxNzY5Nzg1ODg2LCJleHAiOjE3Njk3ODk0ODZ9.eTAh6bR-deRV-Y_1FMRAhXk0BfsQxMy-E0KkHhBuBDk")
    private String refreshToken;
}
