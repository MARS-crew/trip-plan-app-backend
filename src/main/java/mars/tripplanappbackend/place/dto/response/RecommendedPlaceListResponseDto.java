package mars.tripplanappbackend.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "메인 페이지 추천 여행지 목록 응답")
public class RecommendedPlaceListResponseDto {

    @Schema(description = "추천 여행지 개수", example = "5")
    private int placeCount;

    @Schema(description = "추천 여행지 목록")
    private List<RecommendedPlaceResponseDto> recommendedPlaces;
}
