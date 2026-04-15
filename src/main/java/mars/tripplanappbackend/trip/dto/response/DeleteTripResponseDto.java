package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.Trip;

/**
 * 내 여행 삭제 결과를 응답할 때 사용하는 DTO입니다.
 * 화면에서는 삭제 성공 여부와 어떤 여행이 삭제되었는지를 바로 판단할 수 있어야 하므로
 * 여행 PK와 삭제 여부, 함께 정리된 일정 개수를 함께 내려줍니다.
 */
@Getter
@Builder
@Schema(description = "내 여행 삭제 응답 DTO")
public class DeleteTripResponseDto {

    @Schema(description = "삭제한 여행 PK", example = "1")
    private Long tripId;

    @Schema(description = "삭제 처리 여부", example = "true")
    private boolean deleted;

    @Schema(description = "함께 삭제 처리된 일정 개수", example = "3")
    private int deletedScheduleCount;

    /**
     * 삭제 처리된 여행 엔티티와 연관 일정 개수로 삭제 응답 DTO를 생성합니다.
     *
     * @param trip 삭제 처리된 여행 엔티티
     * @param deletedScheduleCount 함께 삭제 처리된 일정 개수
     * @return 내 여행 삭제 응답 DTO
     */
    public static DeleteTripResponseDto from(Trip trip, int deletedScheduleCount) {
        return DeleteTripResponseDto.builder()
                .tripId(trip.getTripId())
                .deleted(Boolean.TRUE.equals(trip.getIsDeleted()))
                .deletedScheduleCount(deletedScheduleCount)
                .build();
    }
}
