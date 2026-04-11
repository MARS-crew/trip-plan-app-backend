package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.TripSchedule;

/**
 * 내 여행 상세 화면에서 개별 일정 삭제 결과를 응답할 때 사용하는 DTO입니다.
 * 어떤 여행의 어떤 일정이 삭제되었는지와 삭제 성공 여부를 함께 내려줍니다.
 */
@Getter
@Builder
@Schema(description = "개별 일정 삭제 응답 DTO")
public class DeleteTripScheduleResponseDto {

    @Schema(description = "일정이 속한 여행 PK", example = "7")
    private Long tripId;

    @Schema(description = "삭제한 일정 PK", example = "21")
    private Long tripScheduleId;

    @Schema(description = "삭제 처리 여부", example = "true")
    private boolean deleted;

    /**
     * 삭제 처리된 여행 일정 엔티티로 개별 일정 삭제 응답 DTO를 생성합니다.
     *
     * @param tripSchedule 삭제 처리된 여행 일정 엔티티
     * @return 개별 일정 삭제 응답 DTO
     */
    public static DeleteTripScheduleResponseDto from(TripSchedule tripSchedule) {
        return DeleteTripScheduleResponseDto.builder()
                .tripId(tripSchedule.getTrip().getTripId())
                .tripScheduleId(tripSchedule.getTripScheduleId())
                .deleted(Boolean.TRUE.equals(tripSchedule.getIsDeleted()))
                .build();
    }
}
