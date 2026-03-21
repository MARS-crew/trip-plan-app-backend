package mars.tripplanappbackend.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.place.enums.PlaceType;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@Schema(description = "메인 페이지 추천 여행지 항목")
public class RecommendedPlaceResponseDto {

    @Schema(description = "장소 PK", example = "7")
    private Long placeId;

    @Schema(description = "장소명", example = "해운대 해수욕장")
    private String name;

    @Schema(description = "국가명", example = "대한민국")
    private String countryName;

    @Schema(description = "도시명", example = "부산")
    private String cityName;

    @Schema(description = "대표 이미지 URL", example = "https://cdn.lets-trip.com/place/haeundae.jpg")
    private String imageUrl;

    @Schema(description = "장소 유형", example = "BEACH")
    private PlaceType placeType;

    @Schema(description = "평균 평점", example = "4.8")
    private BigDecimal ratingAvg;

    @Schema(description = "리뷰 수", example = "128")
    private Integer reviewCount;

    @Schema(description = "대표 태그 목록", example = "[\"바다\", \"야경\", \"산책\"]")
    private List<String> tags;
}
