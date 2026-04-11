package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import mars.tripplanappbackend.mypage.domain.SavedPlace;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.enums.PlaceType;
import mars.tripplanappbackend.trip.domain.WishlistPlace;

import java.math.BigDecimal;

/**
 * 장소 선택 화면에서 저장한 장소와 위시리스트 카드에 공통으로 사용할 장소 항목 DTO입니다.
 */
@Schema(description = "저장한 장소/위시리스트 공통 장소 항목 DTO")
public class TripPlaceSelectionItemResponseDto {

    @Schema(description = "목록 항목 PK", example = "11")
    private final Long selectionId;

    @Schema(description = "장소 PK", example = "7")
    private final Long placeId;

    @Schema(description = "장소명", example = "삿포로 시계탑")
    private final String name;

    @Schema(description = "국가명", example = "일본")
    private final String countryName;

    @Schema(description = "도시명", example = "삿포로")
    private final String cityName;

    @Schema(description = "주소", example = "삿포로 시계탑")
    private final String address;

    @Schema(description = "대표 이미지 URL", example = "https://cdn.lets-trip.com/place/sapporo-clock-tower.jpg")
    private final String imageUrl;

    @Schema(description = "장소 유형", example = "LANDMARK")
    private final PlaceType placeType;

    @Schema(description = "평균 평점", example = "4.6")
    private final BigDecimal ratingAvg;

    @Schema(description = "리뷰 수", example = "312")
    private final Integer reviewCount;

    private TripPlaceSelectionItemResponseDto(
            Long selectionId,
            Long placeId,
            String name,
            String countryName,
            String cityName,
            String address,
            String imageUrl,
            PlaceType placeType,
            BigDecimal ratingAvg,
            Integer reviewCount
    ) {
        this.selectionId = selectionId;
        this.placeId = placeId;
        this.name = name;
        this.countryName = countryName;
        this.cityName = cityName;
        this.address = address;
        this.imageUrl = imageUrl;
        this.placeType = placeType;
        this.ratingAvg = ratingAvg;
        this.reviewCount = reviewCount;
    }

    public Long getSelectionId() {
        return selectionId;
    }

    public Long getPlaceId() {
        return placeId;
    }

    public String getName() {
        return name;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getCityName() {
        return cityName;
    }

    public String getAddress() {
        return address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public PlaceType getPlaceType() {
        return placeType;
    }

    public BigDecimal getRatingAvg() {
        return ratingAvg;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    /**
     * 저장한 장소 엔티티를 공통 장소 카드 응답 DTO로 변환합니다.
     *
     * @param savedPlace 저장한 장소 엔티티
     * @return 장소 카드 응답 DTO
     */
    public static TripPlaceSelectionItemResponseDto fromSavedPlace(SavedPlace savedPlace) {
        return fromPlace(savedPlace.getSavedPlaceId(), savedPlace.getPlace());
    }

    /**
     * 위시리스트 장소 엔티티를 공통 장소 카드 응답 DTO로 변환합니다.
     *
     * @param wishlistPlace 위시리스트 장소 엔티티
     * @return 장소 카드 응답 DTO
     */
    public static TripPlaceSelectionItemResponseDto fromWishlistPlace(WishlistPlace wishlistPlace) {
        return fromPlace(wishlistPlace.getWishlistPlaceId(), wishlistPlace.getPlace());
    }

    private static TripPlaceSelectionItemResponseDto fromPlace(Long selectionId, Place place) {
        return new TripPlaceSelectionItemResponseDto(
                selectionId,
                place.getPlaceId(),
                place.getName(),
                place.getCountryName(),
                place.getCityName(),
                place.getAddress(),
                place.getImageUrl(),
                place.getPlaceType(),
                place.getRatingAvg(),
                place.getReviewCount()
        );
    }
}
