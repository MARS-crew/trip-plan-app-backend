package mars.tripplanappbackend.mypage.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.enums.PlaceType;
import mars.tripplanappbackend.trip.domain.VisitedPlace;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VisitedPlaceResponseDto {
    @Schema(description = "방문 기록 PK")
    private Long visitedPlaceId;

    @Schema(description = "방문 일시")
    private LocalDateTime visitedAt;

    @Schema(description = "장소명")
    private String placeName;

    @Schema(description = "도시명")
    private String cityName;

    @Schema(description = "국가명")
    private String countryName;

    @Schema(description = "이미지 URL")
    private String imageUrl;

    @Schema(description = "장소 유형")
    private PlaceType placeType;

    public VisitedPlaceResponseDto(VisitedPlace visitedPlace) {
        Place place = visitedPlace.getPlace();

        this.visitedPlaceId = visitedPlace.getVisitedPlaceId();
        this.visitedAt = visitedPlace.getVisitedAt();
        this.placeName = place.getName();
        this.cityName = place.getCityName();
        this.countryName = place.getCountryName();
        this.imageUrl = place.getImageUrl();
        this.placeType = place.getPlaceType();
    }
}
