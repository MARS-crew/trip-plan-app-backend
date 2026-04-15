package mars.tripplanappbackend.trip.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 여행 날짜 수정 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class UpdateTripDateResponseDto {

    // 여행 ID
    private Long tripId;

    // 시작 날짜
    private String startDate;

    // 종료 날짜
    private String endDate;
}