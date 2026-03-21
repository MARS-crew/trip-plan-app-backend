package mars.tripplanappbackend.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.place.enums.PlaceType;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@Schema(description = "여행지 상세 조회 응답")
public class PlaceDetailResponseDto {

    @Schema(description = "장소 PK", example = "7")
    private Long placeId;

    @Schema(description = "장소명", example = "해운대 해수욕장")
    private String name;

    @Schema(description = "국가명", example = "대한민국")
    private String countryName;

    @Schema(description = "도시명", example = "부산")
    private String cityName;

    @Schema(description = "주소", example = "부산 해운대구 우동")
    private String address;

    @Schema(description = "장소 설명", example = "부산을 대표하는 해변으로 산책과 야경을 즐기기 좋은 명소입니다.")
    private String description;

    @Schema(description = "위도", example = "35.1586980")
    private BigDecimal latitude;

    @Schema(description = "경도", example = "129.1603840")
    private BigDecimal longitude;

    @Schema(description = "평균 평점", example = "4.8")
    private BigDecimal ratingAvg;

    @Schema(description = "리뷰 수", example = "128")
    private Integer reviewCount;

    @Schema(description = "운영 시간", example = "매일 09:00 - 21:00")
    private String openingHours;

    @Schema(description = "대표 이미지 URL", example = "https://cdn.lets-trip.com/place/haeundae.jpg")
    private String imageUrl;

    @Schema(description = "장소 유형", example = "BEACH")
    private PlaceType placeType;

    @Schema(description = "저장 여부", example = "true")
    private boolean saved;

    @Schema(description = "대표 태그 목록", example = "[\"바다\", \"야경\", \"산책\"]")
    private List<String> tags;

    @Schema(description = "리뷰 미리보기 목록")
    private List<PlaceReviewPreviewResponseDto> reviewPreviews;
}
