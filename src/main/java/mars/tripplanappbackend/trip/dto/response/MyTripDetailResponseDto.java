package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.Trip;
import mars.tripplanappbackend.trip.enums.TripStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * 내 여행 상세 화면 전체를 구성하기 위한 응답 DTO입니다.
 * 여행 기본 정보, 현재 진행 중 일정 요약, 화면 액션 가능 상태, 일차별 일정 섹션 목록을 함께 반환합니다.
 */
@Getter
@Builder
@Schema(description = "내 여행 상세 조회 응답 DTO")
public class MyTripDetailResponseDto {

    @Schema(description = "여행 PK", example = "7")
    private Long tripId;

    @Schema(description = "여행 제목", example = "오사카 여행")
    private String tripTitle;

    @Schema(description = "여행 대표 이미지 URL", example = "https://cdn.lets-trip.com/trips/osaka.jpg", nullable = true)
    private String imageUrl;

    @Schema(description = "여행 상태 코드", example = "ONGOING")
    private TripStatus tripStatus;

    @Schema(description = "여행 상태 라벨", example = "여행 중")
    private String tripStatusLabel;

    @Schema(description = "여행 시작일", example = "2026-04-10")
    private LocalDate startDate;

    @Schema(description = "여행 종료일", example = "2026-04-14")
    private LocalDate endDate;

    @Schema(description = "여행 총 일수", example = "5")
    private long tripDayCount;

    @Schema(description = "전체 일정 개수", example = "8")
    private int totalScheduleCount;

    @Schema(description = "좌표가 있어 지도 보기에서 바로 사용할 수 있는 일정 개수", example = "5")
    private int locationScheduleCount;

    @Schema(description = "현재 진행 중인 일정이 존재하는지 여부", example = "true")
    private boolean hasCurrentSchedule;

    @Schema(description = "현재 진행 중인 일정 요약 정보", nullable = true)
    private MyTripCurrentScheduleResponseDto currentSchedule;

    @Schema(description = "상세 화면에서 지도 보기 버튼을 활성화할 수 있는지 여부", example = "true")
    private boolean canViewMap;

    @Schema(description = "상세 화면에서 여행 편집이 가능한지 여부", example = "true")
    private boolean canEditTrip;

    @Schema(description = "상세 화면에서 일정 추가가 가능한지 여부", example = "true")
    private boolean canAddSchedule;

    @Schema(description = "일차별 일정 섹션 목록")
    private List<MyTripDailyScheduleResponseDto> dailySchedules;

    /**
     * 여행 엔티티와 상세 화면 구성 정보를 묶어 내 여행 상세 응답 DTO를 생성합니다.
     *
     * @param trip 조회 대상 여행 엔티티
     * @param tripStatus 현재 날짜 기준 여행 상태
     * @param tripDayCount 여행 총 일수
     * @param totalScheduleCount 전체 일정 개수
     * @param locationScheduleCount 지도 보기에서 사용할 수 있는 일정 개수
     * @param currentSchedule 현재 진행 중 일정 요약 정보
     * @param canViewMap 지도 보기 가능 여부
     * @param canEditTrip 여행 편집 가능 여부
     * @param canAddSchedule 일정 추가 가능 여부
     * @param dailySchedules 일차별 일정 섹션 목록
     * @return 내 여행 상세 조회 응답 DTO
     */
    public static MyTripDetailResponseDto of(
            Trip trip,
            TripStatus tripStatus,
            long tripDayCount,
            int totalScheduleCount,
            int locationScheduleCount,
            MyTripCurrentScheduleResponseDto currentSchedule,
            boolean canViewMap,
            boolean canEditTrip,
            boolean canAddSchedule,
            List<MyTripDailyScheduleResponseDto> dailySchedules
    ) {
        return MyTripDetailResponseDto.builder()
                .tripId(trip.getTripId())
                .tripTitle(trip.getTitle())
                .imageUrl(trip.getImageUrl())
                .tripStatus(tripStatus)
                .tripStatusLabel(resolveTripStatusLabel(tripStatus))
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .tripDayCount(tripDayCount)
                .totalScheduleCount(totalScheduleCount)
                .locationScheduleCount(locationScheduleCount)
                .hasCurrentSchedule(currentSchedule != null)
                .currentSchedule(currentSchedule)
                .canViewMap(canViewMap)
                .canEditTrip(canEditTrip)
                .canAddSchedule(canAddSchedule)
                .dailySchedules(dailySchedules)
                .build();
    }

    private static String resolveTripStatusLabel(TripStatus tripStatus) {
        return switch (tripStatus) {
            case PLANNED -> "예정";
            case ONGOING -> "여행 중";
            case COMPLETED -> "종료";
        };
    }
}
