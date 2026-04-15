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

    @Schema(description = "저장한 장소 PK(저장한 장소 탭에서만 값 존재)", example = "11", nullable = true)
    private final Long savedPlaceId;

    @Schema(description = "위시리스트 PK(위시리스트에 담긴 경우 값 존재)", example = "25", nullable = true)
    private final Long wishlistPlaceId;

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

    @Schema(description = "장소 소개글", example = "삿포로의 대표 랜드마크로 사진 촬영 명소입니다.", nullable = true)
    private final String description;

    @Schema(description = "대표 이미지 URL", example = "https://cdn.lets-trip.com/place/sapporo-clock-tower.jpg")
    private final String imageUrl;

    @Schema(description = "장소 유형", example = "LANDMARK")
    private final PlaceType placeType;

    @Schema(description = "평균 평점", example = "4.6")
    private final BigDecimal ratingAvg;

    @Schema(description = "리뷰 수", example = "312")
    private final Integer reviewCount;

    @Schema(description = "현재 여행 위시리스트에 이미 담긴 장소인지 여부", example = "true")
    private final boolean inWishlist;

    @Schema(description = "현재 상태에서 표시할 액션 버튼 라벨", example = "담기")
    private final String actionLabel;

    private TripPlaceSelectionItemResponseDto(
            Long selectionId,
            Long savedPlaceId,
            Long wishlistPlaceId,
            Long placeId,
            String name,
            String countryName,
            String cityName,
            String address,
            String description,
            String imageUrl,
            PlaceType placeType,
            BigDecimal ratingAvg,
            Integer reviewCount,
            boolean inWishlist,
            String actionLabel
    ) {
        this.selectionId = selectionId;
        this.savedPlaceId = savedPlaceId;
        this.wishlistPlaceId = wishlistPlaceId;
        this.placeId = placeId;
        this.name = name;
        this.countryName = countryName;
        this.cityName = cityName;
        this.address = address;
        this.description = description;
        this.imageUrl = imageUrl;
        this.placeType = placeType;
        this.ratingAvg = ratingAvg;
        this.reviewCount = reviewCount;
        this.inWishlist = inWishlist;
        this.actionLabel = actionLabel;
    }

    public Long getSelectionId() {
        return selectionId;
    }

    public Long getSavedPlaceId() {
        return savedPlaceId;
    }

    public Long getWishlistPlaceId() {
        return wishlistPlaceId;
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

    public String getDescription() {
        return description;
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

    public boolean isInWishlist() {
        return inWishlist;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    /**
     * 저장한 장소 엔티티를 저장한 장소 탭 카드 응답 DTO로 변환합니다.
     * 현재 여행 위시리스트에 이미 담긴 장소이면 "취소", 아니면 "담기" 라벨을 설정합니다.
     *
     * @param savedPlace 저장한 장소 엔티티
     * @param wishlistPlaceId 현재 여행 위시리스트에 담긴 경우의 위시리스트 PK
     * @return 저장한 장소 탭 카드 응답 DTO
     */
    public static TripPlaceSelectionItemResponseDto fromSavedPlace(
            SavedPlace savedPlace,
            Long wishlistPlaceId
    ) {
        boolean inWishlist = wishlistPlaceId != null;
        return fromPlace(
                savedPlace.getSavedPlaceId(),
                savedPlace.getSavedPlaceId(),
                wishlistPlaceId,
                savedPlace.getPlace(),
                inWishlist,
                inWishlist ? "취소" : "담기"
        );
    }

    /**
     * 위시리스트 장소 엔티티를 공통 장소 카드 응답 DTO로 변환합니다.
     * 위시리스트 탭에서는 모든 항목에 "제거" 라벨을 설정합니다.
     *
     * @param wishlistPlace 위시리스트 장소 엔티티
     * @return 장소 카드 응답 DTO
     */
    public static TripPlaceSelectionItemResponseDto fromWishlistPlace(WishlistPlace wishlistPlace) {
        return fromPlace(
                wishlistPlace.getWishlistPlaceId(),
                null,
                wishlistPlace.getWishlistPlaceId(),
                wishlistPlace.getPlace(),
                true,
                "제거"
        );
    }

    private static TripPlaceSelectionItemResponseDto fromPlace(
            Long selectionId,
            Long savedPlaceId,
            Long wishlistPlaceId,
            Place place,
            boolean inWishlist,
            String actionLabel
    ) {
        return new TripPlaceSelectionItemResponseDto(
                selectionId,
                savedPlaceId,
                wishlistPlaceId,
                place.getPlaceId(),
                place.getName(),
                place.getCountryName(),
                place.getCityName(),
                place.getAddress(),
                place.getDescription(),
                place.getImageUrl(),
                place.getPlaceType(),
                place.getRatingAvg(),
                place.getReviewCount(),
                inWishlist,
                actionLabel
        );
    }
}
