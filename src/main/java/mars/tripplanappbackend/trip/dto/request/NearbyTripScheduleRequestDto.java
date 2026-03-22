package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가까운 여행일정 조회 요청 DTO.
 */
@Getter
@NoArgsConstructor
@Schema(description = "가까운 여행일정 조회 요청")
public class NearbyTripScheduleRequestDto {

    @NotNull(message = "userId는 필수입니다.")
    @Schema(description = "조회할 사용자 PK", example = "1")
    private Long userId;
}
