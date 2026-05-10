package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.TripSchedule;

import java.time.LocalTime;

/**
 * 내 여행 상세 조회 화면에서 일정 카드 한 건을 표현할 때 사용하는 응답 DTO입니다.
 * 화면에서는 현재 진행 중 여부뿐 아니라 방문지 저장, 길찾기, 편집 가능 여부까지 함께 판단해야 하므로
 * 일정 기본 정보와 액션 상태 값을 한 번에 내려줍니다.
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

    @Schema(description = "현재 일정 카드에서 길찾기를 실행할 수 있는지 여부", example = "true")
    private boolean canSearchRoute;

    @Schema(description = "현재 일정 카드에서 편집이 가능한지 여부", example = "true")
    private boolean canEditSchedule;

    /**
     * 여행 일정 엔티티와 화면 상태 계산 결과를 일정 카드 응답 DTO로 변환합니다.
     * 현재 진행 중 여부와 상세 화면 액션 가능 상태는 서비스 계층에서 계산해 전달합니다.
     *
     * @param tripSchedule 여행 일정 엔티티
     * @param address 상세 화면에서 노출하거나 길찾기 기준으로 사용할 주소
     * @param isCurrent 현재 시간 기준 진행 중 일정 여부
     * @param visited 방문 기록 저장 여부
     * @param canAddVisitedPlace 방문지 저장 버튼 노출 가능 여부
     * @param canSearchRoute 길찾기 가능 여부
     * @param canEditSchedule 일정 편집 가능 여부
     * @return 여행 일정 카드 응답 DTO
     */
    public static MyTripScheduleItemResponseDto from(
            TripSchedule tripSchedule,
            String address,
            boolean isCurrent,
            boolean visited,
            boolean canAddVisitedPlace,
            boolean canSearchRoute,
            boolean canEditSchedule
    ) {
        return MyTripScheduleItemResponseDto.builder()
                .tripScheduleId(tripSchedule.getTripScheduleId())
                .placeId(tripSchedule.getPlace() != null ? tripSchedule.getPlace().getPlaceId() : null)
                .title(tripSchedule.getTitle())
                .placeName(tripSchedule.resolvePlaceName())
                .address(address)
                .startTime(tripSchedule.getStartTime())
                .endTime(tripSchedule.getEndTime())
                .memo(tripSchedule.getMemo())
                .isCurrent(isCurrent)
                .visited(visited)
                .canAddVisitedPlace(canAddVisitedPlace)
                .canSearchRoute(canSearchRoute)
                .canEditSchedule(canEditSchedule)
                .build();
    }
}
