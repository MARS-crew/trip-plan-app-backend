package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 내 여행 상세 화면에서 위시리스트로 추가된 장소 카드를 삭제할 때 사용하는 요청 DTO입니다.
 * 컨트롤러에서 전달받은 여행 PK, 위시리스트 PK, 로그인 사용자 아이디를 하나로 묶어
 * 서비스 계층에 전달하기 위한 용도로 사용합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "위시리스트 장소 삭제 요청 DTO")
public class DeleteWishlistPlaceRequestDto {

    @Schema(hidden = true)
    private Long tripId;

    @Schema(hidden = true)
    private Long wishlistPlaceId;

    @Schema(hidden = true)
    private String usersId;

    /**
     * 경로 변수와 현재 로그인 사용자 정보를 기준으로 위시리스트 장소 삭제 요청 DTO를 생성합니다.
     *
     * @param tripId 위시리스트 장소가 속한 여행 PK
     * @param wishlistPlaceId 삭제할 위시리스트 장소 PK
     * @param usersId 현재 로그인한 사용자 아이디
     * @return 서비스 계층에서 사용할 위시리스트 장소 삭제 요청 DTO
     */
    public static DeleteWishlistPlaceRequestDto of(Long tripId, Long wishlistPlaceId, String usersId) {
        DeleteWishlistPlaceRequestDto requestDto = new DeleteWishlistPlaceRequestDto();
        requestDto.tripId = tripId;
        requestDto.wishlistPlaceId = wishlistPlaceId;
        requestDto.usersId = usersId;
        return requestDto;
    }
}
