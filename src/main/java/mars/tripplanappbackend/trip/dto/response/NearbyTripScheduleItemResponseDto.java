package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@Schema(description = "가까운 여행일정 항목")
public class NearbyTripScheduleItemResponseDto {

    @Schema(description = "여행 일정 PK", example = "11")
    private Long tripScheduleId;

    @Schema(description = "일정 날짜", example = "2026-03-25")
    private LocalDate scheduleDate;

    @Schema(description = "시작 시간", example = "09:00:00")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "11:30:00")
    private LocalTime endTime;

    @Schema(description = "일정 제목", example = "해운대 산책")
    private String title;

    @Schema(description = "장소명", example = "해운대 해수욕장")
    private String placeName;

    @Schema(description = "주소", example = "부산 해운대구 우동")
    private String address;

    @Schema(description = "메모", example = "오전에는 산책하고 점심은 근처 맛집 방문")
    private String memo;
}
