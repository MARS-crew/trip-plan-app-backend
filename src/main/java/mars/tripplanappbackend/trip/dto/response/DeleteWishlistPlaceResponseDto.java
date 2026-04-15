package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.WishlistPlace;

/**
 * 위시리스트 장소 삭제 결과를 응답할 때 사용하는 DTO입니다.
 * 어떤 여행의 어떤 위시리스트 장소가 삭제되었는지와 삭제 성공 여부를 함께 반환합니다.
 */
@Getter
@Builder
@Schema(description = "위시리스트 장소 삭제 응답 DTO")
public class DeleteWishlistPlaceResponseDto {

    @Schema(description = "위시리스트 장소가 속한 여행 PK", example = "5")
    private Long tripId;

    @Schema(description = "삭제한 위시리스트 장소 PK", example = "11")
    private Long wishlistPlaceId;

    @Schema(description = "삭제한 장소 PK", example = "7")
    private Long placeId;

    @Schema(description = "삭제한 장소명", example = "삿포로 시계탑")
    private String placeName;

    @Schema(description = "삭제 처리 여부", example = "true")
    private boolean deleted;

    /**
     * 삭제 처리된 위시리스트 장소 엔티티를 기반으로 응답 DTO를 생성합니다.
     *
     * @param wishlistPlace 삭제 처리된 위시리스트 장소 엔티티
     * @return 위시리스트 장소 삭제 응답 DTO
     */
    public static DeleteWishlistPlaceResponseDto from(WishlistPlace wishlistPlace) {
        return DeleteWishlistPlaceResponseDto.builder()
                .tripId(wishlistPlace.getTrip().getTripId())
                .wishlistPlaceId(wishlistPlace.getWishlistPlaceId())
                .placeId(wishlistPlace.getPlace().getPlaceId())
                .placeName(wishlistPlace.getPlace().getName())
                .deleted(Boolean.TRUE.equals(wishlistPlace.getIsDeleted()))
                .build();
    }
}
