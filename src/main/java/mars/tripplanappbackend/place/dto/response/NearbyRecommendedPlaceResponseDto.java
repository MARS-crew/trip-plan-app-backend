package mars.tripplanappbackend.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.place.domain.Place;

/**
 * 주변 추천 장소 단건 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "주변 추천 장소 단건 응답")
public class NearbyRecommendedPlaceResponseDto {

    @Schema(description = "장소 PK", example = "11")
    private Long placeId;

    @Schema(description = "장소명", example = "도쿄 스카이트리")
    private String name;

    @Schema(description = "국가명", example = "일본")
    private String countryName;

    @Schema(description = "도시명", example = "도쿄")
    private String cityName;

    @Schema(description = "대표 이미지 URL", example = "https://cdn.lets-trip.com/place/skytree.jpg")
    private String imageUrl;

    @Schema(description = "기준 장소와의 대략적인 거리(미터)", example = "830")
    private Long distanceMeters;

    /**
     * 장소 엔티티를 주변 추천 장소 응답 DTO로 변환합니다.
     *
     * @param place 주변 추천 대상 장소 엔티티
     * @param distanceMeters 기준 장소로부터 계산된 거리(미터)
     * @return 주변 추천 장소 응답 DTO
     */
    public static NearbyRecommendedPlaceResponseDto from(Place place, Long distanceMeters) {
        return NearbyRecommendedPlaceResponseDto.builder()
                .placeId(place.getPlaceId())
                .name(place.getName())
                .countryName(place.getCountryName())
                .cityName(place.getCityName())
                .imageUrl(place.getImageUrl())
                .distanceMeters(distanceMeters)
                .build();
    }
}
