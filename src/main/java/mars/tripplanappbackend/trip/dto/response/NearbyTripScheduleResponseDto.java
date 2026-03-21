package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.enums.TripStatus;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@Schema(description = "가까운 여행일정 조회 응답")
public class NearbyTripScheduleResponseDto {

    @Schema(description = "가까운 여행 존재 여부", example = "true")
    private boolean hasNearbyTrip;

    @Schema(description = "여행 PK", example = "3")
    private Long tripId;

    @Schema(description = "여행 제목", example = "김민혁의 부산 여행")
    private String tripTitle;

    @Schema(description = "여행 상태", example = "PLANNED")
    private TripStatus tripStatus;

    @Schema(description = "여행 시작일", example = "2026-03-25")
    private LocalDate startDate;

    @Schema(description = "여행 종료일", example = "2026-03-27")
    private LocalDate endDate;

    @Schema(description = "여행 일수", example = "3")
    private long tripDayCount;

    @Schema(description = "여행 시작까지 남은 일수", example = "4", nullable = true)
    private Long daysUntilTrip;

    @Schema(description = "전체 일정 개수", example = "5")
    private int scheduleCount;

    @Schema(description = "다가오는 일정 목록")
    private List<NearbyTripScheduleItemResponseDto> nextSchedules;

    public static NearbyTripScheduleResponseDto empty() {
        return NearbyTripScheduleResponseDto.builder()
                .hasNearbyTrip(false)
                .tripDayCount(0)
                .daysUntilTrip(null)
                .scheduleCount(0)
                .nextSchedules(List.of())
                .build();
    }
}
