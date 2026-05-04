package mars.tripplanappbackend.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.place.domain.Place;

import java.util.List;

/**
 * 검색 결과 카드 1건 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "검색 결과 카드 1건 응답")
public class SearchResultResponseDto {

    @Schema(description = "장소 PK", example = "7")
    private Long placeId;

    @Schema(description = "장소명", example = "삿포로 시계탑")
    private String name;

    @Schema(description = "국가명", example = "일본")
    private String countryName;

    @Schema(description = "도시명", example = "삿포로")
    private String cityName;

    @Schema(description = "대표 이미지 URL", example = "https://cdn.lets-trip.com/place/sapporo-clock-tower.jpg")
    private String imageUrl;

    @Schema(description = "장소 소개", example = "삿포로의 대표 랜드마크로 사진 촬영 명소입니다.")
    private String description;

    @Schema(description = "검색 결과 카드에 노출할 태그 목록")
    private List<String> tags;

    public static SearchResultResponseDto from(Place place, List<String> tags, String description) {
        return SearchResultResponseDto.builder()
                .placeId(place.getPlaceId())
                .name(place.getName())
                .countryName(place.getCountryName())
                .cityName(place.getCityName())
                .imageUrl(place.getImageUrl())
                .description(description)
                .tags(tags)
                .build();
    }
}
