package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.Trip;
import mars.tripplanappbackend.trip.enums.TripStatus;

@Getter
@Builder
@AllArgsConstructor
public class UpdateTripTitleResponseDto {

    @Schema(description = "여행 PK", example = "1")
    private Long tripId;

    @Schema(description = "수정된 여행 제목", example = "오사카 먹방 여행")
    private String title;

    @Schema(description = "여행 상태", example = "PLANNED")
    private TripStatus tripStatus;

    public static UpdateTripTitleResponseDto from(Trip trip, TripStatus tripStatus) {
        return UpdateTripTitleResponseDto.builder()
                .tripId(trip.getTripId())
                .title(trip.getTitle())
                .tripStatus(tripStatus)
                .build();
    }
}