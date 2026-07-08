package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.enums.PlaceType;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@Schema(description = "위시리스트 실시간 추천 장소 항목")
public class TripWishlistRecommendationPlaceResponseDto {

    @Schema(description = "장소 PK", example = "7")
    private Long placeId;

    @Schema(description = "Google Places 장소 ID", example = "ChIJN1t_tDeuEmsRUsoyG83frY4")
    private String googlePlaceId;

    @Schema(description = "장소명", example = "오타루 운하")
    private String name;

    @Schema(description = "국가명", example = "일본")
    private String countryName;

    @Schema(description = "도시명", example = "오타루")
    private String cityName;

    @Schema(description = "주소", example = "일본 홋카이도 오타루시")
    private String address;

    @Schema(description = "소개 문구", nullable = true)
    private String description;

    @Schema(description = "대표 이미지 URL", example = "https://places.googleapis.com/v1/...")
    private String imageUrl;

    @Schema(description = "장소 유형", example = "ATTRACTION")
    private PlaceType placeType;

    @Schema(description = "Google Places 평균 평점", example = "4.6")
    private BigDecimal ratingAvg;

    @Schema(description = "Google Places 리뷰 수", example = "128")
    private Integer reviewCount;

    @Schema(description = "추천 태그 목록", example = "[\"관광\", \"야경\", \"사진\"]")
    private List<String> tags;

    public static TripWishlistRecommendationPlaceResponseDto from(Place place, List<String> tags) {
        return TripWishlistRecommendationPlaceResponseDto.builder()
                .placeId(place.getPlaceId())
                .googlePlaceId(place.getGooglePlaceId())
                .name(place.getName())
                .countryName(place.getCountryName())
                .cityName(place.getCityName())
                .address(place.getAddress())
                .description(place.getDescription())
                .imageUrl(place.getImageUrl())
                .placeType(place.getPlaceType())
                .ratingAvg(resolveRatingAvg(place))
                .reviewCount(resolveReviewCount(place))
                .tags(tags)
                .build();
    }

    private static BigDecimal resolveRatingAvg(Place place) {
        if (place.getGoogleRatingAvg() != null) {
            return place.getGoogleRatingAvg();
        }
        return place.getRatingAvg() == null ? BigDecimal.ZERO : place.getRatingAvg();
    }

    private static Integer resolveReviewCount(Place place) {
        if (place.getGoogleReviewCount() != null) {
            return place.getGoogleReviewCount();
        }
        return place.getReviewCount() == null ? 0 : place.getReviewCount();
    }
}
