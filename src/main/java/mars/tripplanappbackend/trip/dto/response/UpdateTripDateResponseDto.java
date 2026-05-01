package mars.tripplanappbackend.trip.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.Trip;

@Getter
@Builder
@AllArgsConstructor
public class UpdateTripDateResponseDto {

    private Long tripId;
    private String startDate;
    private String endDate;

    public static UpdateTripDateResponseDto from(Trip trip) {
        return UpdateTripDateResponseDto.builder()
                .tripId(trip.getTripId())
                .startDate(trip.getStartDate().toString())
                .endDate(trip.getEndDate().toString())
                .build();
    }
}