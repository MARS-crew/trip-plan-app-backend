package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 내 여행 상세 화면의 전체 일정 리스트를 응답하기 위한 DTO입니다.
 * 여행 기본 정보와 함께 일차별 일정 섹션 목록을 내려주어 상세 화면 본문을 한 번에 구성할 수 있도록 합니다.
 */
@Getter
@Builder
@Schema(description = "내 여행 일정 리스트 조회 응답")
public class MyTripScheduleListResponseDto {

    @Schema(description = "여행 PK", example = "7")
    private Long tripId;

    @Schema(description = "여행 제목", example = "오사카 여행")
    private String tripTitle;

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
     * 내 여행 상세 화면 전체 일정 리스트 응답 DTO를 생성합니다.
     *
     * @param tripId 여행 PK
     * @param tripTitle 여행 제목
     * @param startDate 여행 시작일
     * @param endDate 여행 종료일
     * @param tripDayCount 여행 총 일수
     * @param totalScheduleCount 전체 일정 개수
     * @param dailySchedules 일차별 일정 섹션 목록
     * @return 내 여행 일정 리스트 조회 응답 DTO
     */
    public static MyTripScheduleListResponseDto of(
            Long tripId,
            String tripTitle,
            LocalDate startDate,
            LocalDate endDate,
            long tripDayCount,
            int totalScheduleCount,
            List<MyTripDailyScheduleResponseDto> dailySchedules
    ) {
        return MyTripScheduleListResponseDto.builder()
                .tripId(tripId)
                .tripTitle(tripTitle)
                .startDate(startDate)
                .endDate(endDate)
                .tripDayCount(tripDayCount)
                .totalScheduleCount(totalScheduleCount)
                .dailySchedules(dailySchedules)
                .build();
    }
}
