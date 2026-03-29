package mars.tripplanappbackend.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 여행지 상세 페이지의 주변 추천 장소 섹션 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "주변 추천 장소 목록 응답")
public class NearbyRecommendedPlaceListResponseDto {

    @Schema(description = "주변 추천 장소 개수", example = "3")
    private int placeCount;

    @Schema(description = "주변 추천 장소 목록")
    private List<NearbyRecommendedPlaceResponseDto> nearbyRecommendedPlaces;

    /**
     * 주변 추천 장소 목록으로 목록 응답 DTO를 생성합니다.
     *
     * @param nearbyRecommendedPlaces 주변 추천 장소 목록
     * @return 주변 추천 장소 목록 응답 DTO
     */
    public static NearbyRecommendedPlaceListResponseDto of(
            List<NearbyRecommendedPlaceResponseDto> nearbyRecommendedPlaces
    ) {
        return NearbyRecommendedPlaceListResponseDto.builder()
                .placeCount(nearbyRecommendedPlaces.size())
                .nearbyRecommendedPlaces(nearbyRecommendedPlaces)
                .build();
    }
}
