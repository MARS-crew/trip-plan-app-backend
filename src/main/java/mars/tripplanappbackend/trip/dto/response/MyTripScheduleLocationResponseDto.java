package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.Trip;
import mars.tripplanappbackend.trip.enums.TripStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * 내 여행 상세의 지도 페이지에서 사용할 일정 위치 조회 응답 DTO입니다.
 * 일정별 좌표 목록뿐 아니라 현재 지도 화면에서 바로 필요한 총 일정 개수,
 * 좌표 보유 일정 개수, 방문 인증 반경도 함께 제공해 프론트엔드 후처리를 줄입니다.
 */
@Getter
@Builder
@Schema(description = "내 여행 일정 위치 조회 응답 DTO")
public class MyTripScheduleLocationResponseDto {

    @Schema(description = "여행 PK", example = "5")
    private Long tripId;

    @Schema(description = "여행 제목", example = "오사카 여행")
    private String tripTitle;

    @Schema(description = "여행 상태 코드", example = "ONGOING")
    private TripStatus tripStatus;

    @Schema(description = "여행 시작일", example = "2026-04-10")
    private LocalDate startDate;

    @Schema(description = "여행 종료일", example = "2026-04-14")
    private LocalDate endDate;

    @Schema(description = "전체 일정 개수", example = "8")
    private int totalScheduleCount;

    @Schema(description = "좌표가 있어 지도에 핀을 찍을 수 있는 일정 개수", example = "5")
    private int locationScheduleCount;

    @Schema(description = "GPS 방문 인증 허용 반경(미터)", example = "1000")
    private long visitVerificationRadiusMeters;

    @Schema(description = "지도 페이지에서 사용할 일정 위치 목록")
    private List<MyTripScheduleLocationItemResponseDto> schedules;

    /**
     * 여행 엔티티와 지도용 일정 위치 목록을 조합해 지도 페이지 응답 DTO를 생성합니다.
     *
     * @param trip 조회한 여행 엔티티
     * @param tripStatus 현재 날짜 기준으로 계산한 여행 상태
     * @param visitVerificationRadiusMeters GPS 방문 인증 허용 반경(미터)
     * @param schedules 지도 페이지에 표시할 일정 위치 목록
     * @return 지도 페이지 응답 DTO
     */
    public static MyTripScheduleLocationResponseDto of(
            Trip trip,
            TripStatus tripStatus,
            long visitVerificationRadiusMeters,
            List<MyTripScheduleLocationItemResponseDto> schedules
    ) {
        int locationScheduleCount = (int) schedules.stream()
                .filter(MyTripScheduleLocationItemResponseDto::isHasLocation)
                .count();

        return MyTripScheduleLocationResponseDto.builder()
                .tripId(trip.getTripId())
                .tripTitle(trip.getTitle())
                .tripStatus(tripStatus)
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .totalScheduleCount(schedules.size())
                .locationScheduleCount(locationScheduleCount)
                .visitVerificationRadiusMeters(visitVerificationRadiusMeters)
                .schedules(schedules)
                .build();
    }
}
