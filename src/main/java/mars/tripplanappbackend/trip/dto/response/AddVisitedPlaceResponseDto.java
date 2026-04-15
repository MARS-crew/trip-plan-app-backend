package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.VisitedPlace;

import java.time.LocalDateTime;

/**
 * 방문 기록 저장 결과를 응답으로 전달할 때 사용하는 DTO입니다.
 * 사용자가 어떤 여행에서 어떤 장소를 방문 기록으로 저장했는지 바로 확인할 수 있도록
 * 화면에서 필요한 핵심 값만 담아 반환합니다.
 */
@Getter
@Builder
@Schema(description = "방문 기록 저장 응답 DTO")
public class AddVisitedPlaceResponseDto {

    @Schema(description = "방문 기록 PK", example = "3")
    private Long visitedPlaceId;

    @Schema(description = "방문 기록이 저장된 여행 PK", example = "5")
    private Long tripId;

    @Schema(description = "방문 기록과 연결된 일정 PK", example = "21", nullable = true)
    private Long tripScheduleId;

    @Schema(description = "방문 기록으로 저장한 장소 PK", example = "7")
    private Long placeId;

    @Schema(description = "방문 기록으로 저장한 장소명", example = "삿포로 시계탑")
    private String placeName;

    @Schema(description = "방문 기록 저장 시각", example = "2026-04-11T15:30:00")
    private LocalDateTime visitedAt;

    @Schema(description = "방문 기록 저장 여부", example = "true")
    private boolean visited;

    /**
     * 저장이 완료된 방문 기록 엔티티를 응답 DTO로 변환합니다.
     *
     * @param visitedPlace 저장이 완료된 방문 기록 엔티티
     * @return 방문 기록 저장 응답 DTO
     */
    public static AddVisitedPlaceResponseDto from(VisitedPlace visitedPlace) {
        return AddVisitedPlaceResponseDto.builder()
                .visitedPlaceId(visitedPlace.getVisitedPlaceId())
                .tripId(visitedPlace.getTrip().getTripId())
                .tripScheduleId(
                        visitedPlace.getTripSchedule() != null ? visitedPlace.getTripSchedule().getTripScheduleId() : null
                )
                .placeId(visitedPlace.getPlace().getPlaceId())
                .placeName(visitedPlace.getPlace().getName())
                .visitedAt(visitedPlace.getVisitedAt())
                .visited(true)
                .build();
    }
}
