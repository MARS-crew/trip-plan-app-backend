package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.Trip;
import mars.tripplanappbackend.trip.enums.TripStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 가까운 여행일정 조회 응답 DTO.
 */
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

    /**
     * 가까운 여행이 없을 때 사용할 기본 응답을 생성한다.
     *
     * @return 비어 있는 가까운 여행일정 응답 DTO
     */
    public static NearbyTripScheduleResponseDto empty() {
        return NearbyTripScheduleResponseDto.builder()
                .hasNearbyTrip(false)
                .tripDayCount(0)
                .daysUntilTrip(null)
                .scheduleCount(0)
                .nextSchedules(List.of())
                .build();
    }

    /**
     * 여행 엔티티와 일정 목록을 메인 카드 응답 DTO로 변환한다.
     *
     * @param trip 선택된 여행 엔티티
     * @param tripStatus 계산된 여행 상태
     * @param today 서버 기준 현재 날짜
     * @param scheduleCount 전체 일정 개수
     * @param nextSchedules 노출할 다음 일정 목록
     * @return 가까운 여행일정 응답 DTO
     */
    public static NearbyTripScheduleResponseDto of(
            Trip trip,
            TripStatus tripStatus,
            LocalDate today,
            int scheduleCount,
            List<NearbyTripScheduleItemResponseDto> nextSchedules
    ) {
        return NearbyTripScheduleResponseDto.builder()
                .hasNearbyTrip(true)
                .tripId(trip.getTripId())
                .tripTitle(trip.getTitle())
                .tripStatus(tripStatus)
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .tripDayCount(ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1)
                .daysUntilTrip(tripStatus == TripStatus.PLANNED
                        ? ChronoUnit.DAYS.between(today, trip.getStartDate())
                        : null)
                .scheduleCount(scheduleCount)
                .nextSchedules(nextSchedules)
                .build();
    }
}
