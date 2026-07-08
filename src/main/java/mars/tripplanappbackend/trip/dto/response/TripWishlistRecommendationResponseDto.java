package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "위시리스트 실시간 추천 장소 조회 응답 DTO")
public class TripWishlistRecommendationResponseDto {

    @Schema(description = "조회한 여행 PK", example = "5")
    private Long tripId;

    @Schema(description = "조회한 여행 제목", example = "삿포로 여행")
    private String tripTitle;

    @Schema(description = "추천 장소 개수", example = "1")
    private int recommendedPlaceCount;

    @Schema(description = "추천 장소 목록이 비어 있는지 여부", example = "false")
    private boolean recommendedPlaceEmpty;

    @Schema(description = "추천 장소가 없을 때 표시할 안내 문구", example = "추천 장소가 없습니다.", nullable = true)
    private String recommendedPlaceEmptyMessage;

    @Schema(description = "위시리스트 실시간 추천 장소 목록")
    private List<TripWishlistRecommendationPlaceResponseDto> recommendedPlaces;

    public static TripWishlistRecommendationResponseDto of(
            Long tripId,
            String tripTitle,
            List<TripWishlistRecommendationPlaceResponseDto> recommendedPlaces
    ) {
        boolean empty = recommendedPlaces.isEmpty();
        return TripWishlistRecommendationResponseDto.builder()
                .tripId(tripId)
                .tripTitle(tripTitle)
                .recommendedPlaceCount(recommendedPlaces.size())
                .recommendedPlaceEmpty(empty)
                .recommendedPlaceEmptyMessage(empty ? "추천 장소가 없습니다." : null)
                .recommendedPlaces(recommendedPlaces)
                .build();
    }
}
