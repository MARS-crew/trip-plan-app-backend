package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding a place to trip wishlist.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "위시리스트 장소 추가 요청 DTO")
public class AddWishlistPlaceRequestDto {

    @Schema(hidden = true)
    private Long tripId;

    @Schema(hidden = true)
    private String usersId;

    @NotNull(message = "추가할 장소 PK는 필수입니다.")
    @Schema(description = "추가할 장소 PK", example = "7")
    private Long placeId;

    public static AddWishlistPlaceRequestDto of(Long tripId, String usersId, AddWishlistPlaceRequestDto requestDto) {
        AddWishlistPlaceRequestDto serviceRequestDto = new AddWishlistPlaceRequestDto();
        serviceRequestDto.tripId = tripId;
        serviceRequestDto.usersId = usersId;
        serviceRequestDto.placeId = requestDto.placeId;
        return serviceRequestDto;
    }
}
