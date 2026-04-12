package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.Trip;
import mars.tripplanappbackend.trip.enums.TripStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * 내 일정 상세 조회 화면 전체를 구성하기 위한 응답 DTO입니다.
 * 여행 기본 정보와 일차별 일정 섹션을 함께 내려주어,
 * 프론트엔드가 여행 헤더와 Day 탭, 일정 리스트를 한 번에 그릴 수 있도록 합니다.
 */
@Getter
@Builder
@Schema(description = "내 일정 상세 조회 응답 DTO")
public class MyTripScheduleListResponseDto {

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

    @Schema(description = "일차별 일정 섹션 목록")
    private List<MyTripDailyScheduleResponseDto> dailySchedules;

    /**
     * 여행 엔티티와 일정 섹션 목록을 일정 상세 조회 응답 DTO로 변환합니다.
     * 상단 헤더에 필요한 여행 상태와 대표 이미지도 함께 내려줍니다.
     *
     * @param trip 조회 대상 여행 엔티티
     * @param tripStatus 현재 날짜 기준 여행 상태
     * @param tripDayCount 여행 총 일수
     * @param totalScheduleCount 전체 일정 개수
     * @param dailySchedules 일차별 일정 섹션 목록
     * @return 내 일정 상세 조회 응답 DTO
     */
    public static MyTripScheduleListResponseDto of(
            Trip trip,
            TripStatus tripStatus,
            long tripDayCount,
            int totalScheduleCount,
            List<MyTripDailyScheduleResponseDto> dailySchedules
    ) {
        return MyTripScheduleListResponseDto.builder()
                .tripId(trip.getTripId())
                .tripTitle(trip.getTitle())
                .imageUrl(trip.getImageUrl())
                .tripStatus(tripStatus)
                .tripStatusLabel(resolveTripStatusLabel(tripStatus))
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .tripDayCount(tripDayCount)
                .totalScheduleCount(totalScheduleCount)
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
