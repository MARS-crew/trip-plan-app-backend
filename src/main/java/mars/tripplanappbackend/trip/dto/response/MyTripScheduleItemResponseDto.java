package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.TripSchedule;

import java.time.LocalTime;

/**
 * 내 일정 상세 조회 화면에서 일정 카드 한 건을 표현할 때 사용하는 응답 DTO입니다.
 * 화면에서는 시간순 일정 목록, 현재 진행 중 여부, 방문지 저장 버튼 노출 여부를 함께 판단해야 하므로
 * 일정 기본 정보와 장소 PK, 현재 상태 관련 값을 한 번에 내려줍니다.
 */
@Getter
@Builder
@Schema(description = "내 여행 일정 카드 응답 DTO")
public class MyTripScheduleItemResponseDto {

    @Schema(description = "여행 일정 PK", example = "21")
    private Long tripScheduleId;

    @Schema(description = "일정에 연결된 장소 PK", example = "7", nullable = true)
    private Long placeId;

    @Schema(description = "일정 제목", example = "공항 이동")
    private String title;

    @Schema(description = "일정 장소명", example = "간사이 국제공항", nullable = true)
    private String placeName;

    @Schema(description = "일정 주소", example = "오사카 공항", nullable = true)
    private String address;

    @Schema(description = "시작 시간", example = "09:00:00")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "10:00:00")
    private LocalTime endTime;

    @Schema(description = "일정 메모", example = "공항 이동 후 체크인", nullable = true)
    private String memo;

    @Schema(description = "현재 시각과 겹치는 진행 중 일정인지 여부", example = "true")
    private boolean isCurrent;

    @Schema(description = "해당 일정의 장소가 이미 방문 기록으로 저장되었는지 여부", example = "false")
    private boolean visited;

    @Schema(description = "현재 일정 카드에서 방문지 저장 버튼을 노출할 수 있는지 여부", example = "true")
    private boolean canAddVisitedPlace;

    /**
     * 여행 일정 엔티티와 화면 상태 계산 결과를 일정 카드 응답 DTO로 변환합니다.
     * 현재 진행 중 여부와 방문 기록 저장 가능 여부는 서비스 계층에서 계산해 전달합니다.
     *
     * @param tripSchedule 여행 일정 엔티티
     * @param isCurrent 현재 시간 기준 진행 중 일정 여부
     * @param visited 방문 기록 저장 여부
     * @param canAddVisitedPlace 방문지 저장 버튼 노출 가능 여부
     * @return 여행 일정 카드 응답 DTO
     */
    public static MyTripScheduleItemResponseDto from(
            TripSchedule tripSchedule,
            boolean isCurrent,
            boolean visited,
            boolean canAddVisitedPlace
    ) {
        return MyTripScheduleItemResponseDto.builder()
                .tripScheduleId(tripSchedule.getTripScheduleId())
                .placeId(tripSchedule.getPlace() != null ? tripSchedule.getPlace().getPlaceId() : null)
                .title(tripSchedule.getTitle())
                .placeName(tripSchedule.getPlace() != null ? tripSchedule.getPlace().getName() : null)
                .address(tripSchedule.getAddress())
                .startTime(tripSchedule.getStartTime())
                .endTime(tripSchedule.getEndTime())
                .memo(tripSchedule.getMemo())
                .isCurrent(isCurrent)
                .visited(visited)
                .canAddVisitedPlace(canAddVisitedPlace)
                .build();
    }

    /**
     * 현재 상태 계산이 필요하지 않은 기존 호출부와의 호환을 위해 기본 상태값 false로 변환합니다.
     *
     * @param tripSchedule 여행 일정 엔티티
     * @return 기본 상태값이 채워진 일정 카드 응답 DTO
     */
    public static MyTripScheduleItemResponseDto from(TripSchedule tripSchedule) {
        return from(tripSchedule, false, false, false);
    }
}
