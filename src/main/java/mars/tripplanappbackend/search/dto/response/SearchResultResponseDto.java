package mars.tripplanappbackend.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.place.domain.Place;

import java.math.BigDecimal;
import java.util.List;

/**
 * 검색 결과 리스트 단건 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "검색 결과 리스트 단건 응답")
public class SearchResultResponseDto {

    @Schema(description = "장소 PK", example = "7")
    private Long placeId;

    @Schema(description = "장소명", example = "후카이도청")
    private String name;

    @Schema(description = "국가명", example = "일본")
    private String countryName;

    @Schema(description = "도시명", example = "삿포로")
    private String cityName;

    @Schema(description = "대표 이미지 URL", example = "https://cdn.lets-trip.com/place/hokkaido-office.jpg")
    private String imageUrl;

    @Schema(description = "평균 별점", example = "4.6")
    private BigDecimal ratingAvg;

    @Schema(description = "리뷰 수", example = "56789")
    private Integer reviewCount;

    @Schema(description = "검색 결과 카드에 노출할 태그 목록")
    private List<String> tags;

    /**
     * 장소 엔티티와 태그 목록을 검색 결과 응답 DTO로 변환합니다.
     *
     * @param place 장소 엔티티
     * @param tags 장소 태그 목록
     * @return 검색 결과 리스트 단건 응답 DTO
     */
    public static SearchResultResponseDto from(Place place, List<String> tags) {
        return SearchResultResponseDto.builder()
                .placeId(place.getPlaceId())
                .name(place.getName())
                .countryName(place.getCountryName())
                .cityName(place.getCityName())
                .imageUrl(place.getImageUrl())
                .ratingAvg(place.getRatingAvg())
                .reviewCount(place.getReviewCount())
                .tags(tags)
                .build();
    }
}
