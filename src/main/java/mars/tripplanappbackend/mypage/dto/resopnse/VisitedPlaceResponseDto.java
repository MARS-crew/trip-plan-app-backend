package mars.tripplanappbackend.mypage.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.global.enums.UseYnEnum;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.enums.PlaceType;
import mars.tripplanappbackend.trip.domain.VisitedPlace;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VisitedPlaceResponseDto {

    @Schema(description = "Visited place record PK", example = "1")
    private Long visitedPlaceId;

    @Schema(description = "Place PK", example = "8")
    private Long placeId;

    @Schema(description = "Visited timestamp", example = "2026-04-12T14:30:00")
    private LocalDateTime visitedAt;

    @Schema(description = "Place name", example = "Eiffel Tower")
    private String placeName;

    @Schema(description = "City name", example = "Paris")
    private String cityName;

    @Schema(description = "Country name", example = "France")
    private String countryName;

    @Schema(description = "Image URL", example = "places/1/a.png")
    private String imageUrl;

    @Schema(description = "Place type", example = "LANDMARK")
    private PlaceType placeType;

    @Schema(description = "Whether a review has been written", example = "Y")
    private UseYnEnum reviewWrittenYn;

    public VisitedPlaceResponseDto(VisitedPlace visitedPlace, UseYnEnum reviewWrittenYn) {
        Place place = visitedPlace.getPlace();

        this.visitedPlaceId = visitedPlace.getVisitedPlaceId();
        this.placeId = place.getPlaceId();
        this.visitedAt = visitedPlace.getVisitedAt();
        this.placeName = place.getName();
        this.cityName = place.getCityName();
        this.countryName = place.getCountryName();
        this.imageUrl = place.getImageUrl();
        this.placeType = place.getPlaceType();
        this.reviewWrittenYn = reviewWrittenYn;
    }
}
