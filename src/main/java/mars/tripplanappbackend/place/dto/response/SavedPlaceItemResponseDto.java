package mars.tripplanappbackend.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.mypage.domain.SavedPlace;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.enums.PlaceType;

import java.math.BigDecimal;
import java.util.List;

/**
 * 저장한 장소 카드 한 장에 필요한 정보를 담는 응답 DTO입니다.
 * 저장 탭 목록, 여행 추가 바텀시트의 저장한 장소 탭 등에서 공통으로 재사용합니다.
 */
@Getter
@Builder
@Schema(description = "저장한 장소 카드 응답 DTO")
public class SavedPlaceItemResponseDto {

    @Schema(description = "저장 항목 PK", example = "15")
    private Long savedPlaceId;

    @Schema(description = "장소 상세 재진입에 사용할 장소 PK", example = "7")
    private Long placeId;

    @Schema(description = "장소명", example = "삿포로 시계탑")
    private String name;

    @Schema(description = "카드에 노출할 위치 문자열", example = "일본 삿포로")
    private String location;

    @Schema(description = "국가명", example = "일본")
    private String countryName;

    @Schema(description = "도시명", example = "삿포로")
    private String cityName;

    @Schema(description = "상세 주소", example = "삿포로 시계탑")
    private String address;

    @Schema(description = "대표 이미지 URL", example = "https://cdn.lets-trip.com/place/sapporo-clock-tower.jpg")
    private String imageUrl;

    @Schema(description = "장소 유형", example = "LANDMARK")
    private PlaceType placeType;

    @Schema(description = "평균 별점", example = "4.5")
    private BigDecimal ratingAvg;

    @Schema(description = "리뷰 수", example = "1200")
    private Integer reviewCount;

    @Schema(description = "태그 목록", example = "[\"역사\", \"관광\", \"시내\"]")
    private List<String> tags;

    @Schema(description = "저장 여부", example = "true")
    private boolean saved;

    /**
     * 저장한 장소 엔티티와 태그 목록을 저장한 장소 카드 응답 DTO로 변환합니다.
     *
     * @param savedPlace 저장한 장소 엔티티
     * @param tags 카드에 노출할 태그 목록
     * @return 저장한 장소 카드 응답 DTO
     */
    public static SavedPlaceItemResponseDto from(SavedPlace savedPlace, List<String> tags) {
        Place place = savedPlace.getPlace();

        return SavedPlaceItemResponseDto.builder()
                .savedPlaceId(savedPlace.getSavedPlaceId())
                .placeId(place.getPlaceId())
                .name(place.getName())
                .location(buildLocation(place))
                .countryName(place.getCountryName())
                .cityName(place.getCityName())
                .address(place.getAddress())
                .imageUrl(place.getImageUrl())
                .placeType(place.getPlaceType())
                .ratingAvg(place.getRatingAvg())
                .reviewCount(place.getReviewCount())
                .tags(tags)
                .saved(true)
                .build();
    }

    private static String buildLocation(Place place) {
        if (place.getCountryName() != null && place.getCityName() != null
                && !place.getCountryName().isBlank() && !place.getCityName().isBlank()) {
            return place.getCountryName() + " " + place.getCityName();
        }

        if (place.getCountryName() != null && !place.getCountryName().isBlank()) {
            return place.getCountryName();
        }

        if (place.getCityName() != null && !place.getCityName().isBlank()) {
            return place.getCityName();
        }

        return place.getAddress();
    }
}
