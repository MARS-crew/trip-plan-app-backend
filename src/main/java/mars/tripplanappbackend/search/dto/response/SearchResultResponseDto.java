package mars.tripplanappbackend.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.enums.PlaceType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Search result card response DTO.
 */
@Getter
@Builder
@Schema(description = "Search result item response")
public class SearchResultResponseDto {

    @Schema(description = "Place PK", example = "7")
    private Long placeId;

    @Schema(description = "Place name", example = "Sapporo Clock Tower")
    private String name;

    @Schema(description = "Country name", example = "Japan")
    private String countryName;

    @Schema(description = "City name", example = "Sapporo")
    private String cityName;

    @Schema(description = "Place image URL", example = "https://cdn.lets-trip.com/place/sapporo-clock-tower.jpg")
    private String imageUrl;

    @Schema(description = "Place description", example = "A landmark in central Sapporo.")
    private String description;

    @Schema(description = "App category mapped from Google Places type", example = "ATTRACTION")
    private PlaceType placeType;

    @Schema(description = "Latitude", example = "43.0621000")
    private BigDecimal latitude;

    @Schema(description = "Longitude", example = "141.3544000")
    private BigDecimal longitude;

    @Schema(description = "Average rating", example = "4.5")
    private BigDecimal ratingAvg;

    @Schema(description = "Review count", example = "1201")
    private Integer reviewCount;

    @Schema(description = "Tags displayed on the search result card")
    private List<String> tags;

    public static SearchResultResponseDto from(Place place, List<String> tags, String description) {
        return SearchResultResponseDto.builder()
                .placeId(place.getPlaceId())
                .name(place.getName())
                .countryName(place.getCountryName())
                .cityName(place.getCityName())
                .imageUrl(place.getImageUrl())
                .description(description)
                .placeType(PlaceType.normalizeForAppCategory(place.getPlaceType()))
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .ratingAvg(resolveRatingAvg(place))
                .reviewCount(resolveReviewCount(place))
                .tags(tags)
                .build();
    }

    private static BigDecimal resolveRatingAvg(Place place) {
        Integer reviewCount = resolveReviewCount(place);
        if (reviewCount == 0 || place.getRatingAvg() == null) {
            return BigDecimal.ZERO;
        }
        return place.getRatingAvg();
    }

    private static Integer resolveReviewCount(Place place) {
        if (place.getReviewCount() == null || place.getReviewCount() <= 0) {
            return 0;
        }
        return place.getReviewCount();
    }
}
