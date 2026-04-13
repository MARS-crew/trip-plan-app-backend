package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.place.domain.Place;

import java.math.BigDecimal;

/**
 * 내 여행지 상세 지도 검색 결과의 개별 장소 카드/핀 항목 응답 DTO입니다.
 * 지도 핀 표시에 필요한 좌표와, 리스트 카드 렌더링에 필요한 텍스트/이미지/액션 상태를 함께 담습니다.
 */
@Getter
@Builder
@Schema(description = "내 여행지 상세 지도 검색 결과 항목 DTO")
public class MyTripMapSearchItemResponseDto {

    private static final String ACTION_ADD = "담기";
    private static final String ACTION_CANCEL = "취소";

    @Schema(description = "장소 PK", example = "7")
    private Long placeId;

    @Schema(description = "현재 여행 위시리스트 PK(이미 담긴 경우만 값 존재)", example = "25", nullable = true)
    private Long wishlistPlaceId;

    @Schema(description = "장소명", example = "오사카성")
    private String placeName;

    @Schema(description = "주소", example = "1-1 Osakajo, Chuo Ward, Osaka")
    private String address;

    @Schema(description = "장소 소개글", example = "오사카를 대표하는 랜드마크입니다.", nullable = true)
    private String description;

    @Schema(description = "대표 이미지 URL", example = "https://cdn.lets-trip.com/place/osaka-castle.jpg", nullable = true)
    private String imageUrl;

    @Schema(description = "위도", example = "34.6873150", nullable = true)
    private BigDecimal latitude;

    @Schema(description = "경도", example = "135.5262010", nullable = true)
    private BigDecimal longitude;

    @Schema(description = "지도 핀 표시 가능 여부(위도/경도 모두 존재해야 true)", example = "true")
    private boolean hasCoordinate;

    @Schema(description = "현재 여행 위시리스트에 이미 담긴 장소인지 여부", example = "false")
    private boolean inWishlist;

    @Schema(description = "현재 상태에서 표시할 액션 버튼 라벨", example = "담기")
    private String actionLabel;

    /**
     * 장소 엔티티와 위시리스트 포함 여부를 기반으로
     * 지도 검색 결과 항목 응답 DTO를 생성합니다.
     *
     * @param place 검색 결과 장소 엔티티
     * @param wishlistPlaceId 현재 여행 위시리스트 PK(없으면 null)
     * @return 지도 검색 결과 항목 응답 DTO
     */
    public static MyTripMapSearchItemResponseDto from(Place place, Long wishlistPlaceId) {
        boolean inWishlist = wishlistPlaceId != null;
        BigDecimal latitude = place.getLatitude();
        BigDecimal longitude = place.getLongitude();

        return MyTripMapSearchItemResponseDto.builder()
                .placeId(place.getPlaceId())
                .wishlistPlaceId(wishlistPlaceId)
                .placeName(place.getName())
                .address(place.getAddress())
                .description(place.getDescription())
                .imageUrl(place.getImageUrl())
                .latitude(latitude)
                .longitude(longitude)
                .hasCoordinate(latitude != null && longitude != null)
                .inWishlist(inWishlist)
                .actionLabel(inWishlist ? ACTION_CANCEL : ACTION_ADD)
                .build();
    }
}

