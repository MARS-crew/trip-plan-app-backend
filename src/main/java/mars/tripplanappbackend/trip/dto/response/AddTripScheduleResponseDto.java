package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.trip.domain.TripSchedule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@Schema(description = "Response DTO for adding a trip schedule")
public class AddTripScheduleResponseDto {

    @Schema(description = "Trip id", example = "5")
    private Long tripId;

    @Schema(description = "Trip schedule id", example = "21")
    private Long tripScheduleId;

    @Schema(description = "Day number from trip start date", example = "2")
    private int dayNo;

    @Schema(description = "Schedule date", example = "2026-03-02")
    private LocalDate scheduleDate;

    @Schema(description = "Schedule title", example = "Dotonbori dinner")
    private String title;

    @Schema(description = "Schedule start time", example = "11:30")
    private LocalTime startTime;

    @Schema(description = "Schedule end time", example = "12:30")
    private LocalTime endTime;

    @Schema(description = "Place id", example = "7", nullable = true)
    private Long placeId;

    @Schema(description = "Place name", example = "Dotonbori", nullable = true)
    private String placeName;

    @Schema(description = "Place latitude", example = "34.6937249", nullable = true)
    private BigDecimal latitude;

    @Schema(description = "Place longitude", example = "135.5022535", nullable = true)
    private BigDecimal longitude;

    @Schema(description = "Schedule memo", example = "Wait time can be long", nullable = true)
    private String memo;

    @Schema(description = "Whether the schedule was added successfully", example = "true")
    private boolean added;

    public static AddTripScheduleResponseDto from(TripSchedule tripSchedule) {
        Place place = tripSchedule.getPlace();

        return AddTripScheduleResponseDto.builder()
                .tripId(tripSchedule.getTrip().getTripId())
                .tripScheduleId(tripSchedule.getTripScheduleId())
                .dayNo(tripSchedule.getDayNo())
                .scheduleDate(tripSchedule.getScheduleDate())
                .title(tripSchedule.getTitle())
                .startTime(tripSchedule.getStartTime())
                .endTime(tripSchedule.getEndTime())
                .placeId(place != null ? place.getPlaceId() : null)
                .placeName(place != null ? place.getName() : null)
                .latitude(place != null ? place.getLatitude() : null)
                .longitude(place != null ? place.getLongitude() : null)
                .memo(tripSchedule.getMemo())
                .added(true)
                .build();
    }
}
