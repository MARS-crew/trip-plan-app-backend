package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UpdateTripDateRequestDto {

    @Schema(description = "여행 시작 날짜", example = "2026-05-01")
    private LocalDate startDate;

    @Schema(description = "여행 종료 날짜", example = "2026-05-03")
    private LocalDate endDate;
}