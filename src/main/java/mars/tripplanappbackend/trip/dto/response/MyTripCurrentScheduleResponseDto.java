package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.TripSchedule;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 내 여행 상세 화면에서 현재 진행 중인 일정을 빠르게 표시하기 위한 요약 DTO입니다.
 * 화면 상단 또는 고정 액션 영역에서 현재 일정 정보와 즉시 수행 가능한 액션 상태를 함께 사용할 수 있습니다.
 */
@Getter
@Builder
@Schema(description = "현재 진행 중인 일정 요약 응답 DTO")
public class MyTripCurrentScheduleResponseDto {

    @Schema(description = "여행 일정 PK", example = "21")
    private Long tripScheduleId;

    @Schema(description = "현재 일정이 속한 일차 번호", example = "2")
    private Integer dayNo;

    @Schema(description = "현재 일정 날짜", example = "2026-04-11")
    private LocalDate scheduleDate;

    @Schema(description = "현재 일정 일차 라벨", example = "2일차")
    private String dayLabel;

    @Schema(description = "연결된 장소 PK", example = "7", nullable = true)
    private Long placeId;

    @Schema(description = "현재 일정 제목", example = "오사카성 방문")
    private String title;

    @Schema(description = "현재 일정 장소명", example = "오사카성", nullable = true)
    private String placeName;

    @Schema(description = "현재 일정 기준 주소", example = "1-1 Osakajo, Chuo Ward, Osaka", nullable = true)
    private String address;

    @Schema(description = "현재 일정 시작 시간", example = "10:00:00")
    private LocalTime startTime;

    @Schema(description = "현재 일정 종료 시간", example = "12:00:00")
    private LocalTime endTime;

    @Schema(description = "이미 방문 기록으로 저장된 장소인지 여부", example = "false")
    private boolean visited;

    @Schema(description = "현재 일정 기준 방문지 저장이 가능한지 여부", example = "true")
    private boolean canAddVisitedPlace;

    @Schema(description = "현재 일정 기준 길찾기가 가능한지 여부", example = "true")
    private boolean canSearchRoute;

    /**
     * 현재 진행 중 일정 엔티티와 액션 상태를 묶어 요약 응답 DTO를 생성합니다.
     *
     * @param tripSchedule 현재 진행 중 일정 엔티티
     * @param address 상세 화면에서 사용할 기준 주소
     * @param visited 방문 기록 저장 여부
     * @param canAddVisitedPlace 방문지 저장 가능 여부
     * @param canSearchRoute 길찾기 가능 여부
     * @return 현재 진행 중 일정 요약 응답 DTO
     */
    public static MyTripCurrentScheduleResponseDto of(
            TripSchedule tripSchedule,
            String address,
            boolean visited,
            boolean canAddVisitedPlace,
            boolean canSearchRoute
    ) {
        return MyTripCurrentScheduleResponseDto.builder()
                .tripScheduleId(tripSchedule.getTripScheduleId())
                .dayNo(tripSchedule.getDayNo())
                .scheduleDate(tripSchedule.getScheduleDate())
                .dayLabel(tripSchedule.getDayNo() + "일차")
                .placeId(tripSchedule.getPlace() != null ? tripSchedule.getPlace().getPlaceId() : null)
                .title(tripSchedule.getTitle())
                .placeName(tripSchedule.resolvePlaceName())
                .address(address)
                .startTime(tripSchedule.getStartTime())
                .endTime(tripSchedule.getEndTime())
                .visited(visited)
                .canAddVisitedPlace(canAddVisitedPlace)
                .canSearchRoute(canSearchRoute)
                .build();
    }
}
