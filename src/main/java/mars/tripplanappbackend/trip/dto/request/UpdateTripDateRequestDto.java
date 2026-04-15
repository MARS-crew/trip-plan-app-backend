package mars.tripplanappbackend.trip.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 여행 날짜 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
public class UpdateTripDateRequestDto {

    // 여행 시작 날짜
    private LocalDate startDate;

    // 여행 종료 날짜
    private LocalDate endDate;
}