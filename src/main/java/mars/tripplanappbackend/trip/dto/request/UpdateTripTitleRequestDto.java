package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UpdateTripTitleRequestDto {

    @Schema(description = "여행 PK", example = "1")
    private Long tripId;

    @Schema(description = "로그인 사용자 아이디", example = "apie0711")
    private String usersId;

    @NotBlank(message = "여행 제목은 비어 있을 수 없습니다.")
    @Schema(description = "수정할 여행 제목", example = "오사카 먹방 여행")
    private String title;

    public static UpdateTripTitleRequestDto of(
            Long tripId,
            String usersId,
            UpdateTripTitleRequestDto requestDto
    ) {
        return new UpdateTripTitleRequestDto(
                tripId,
                usersId,
                requestDto.getTitle()
        );
    }
}