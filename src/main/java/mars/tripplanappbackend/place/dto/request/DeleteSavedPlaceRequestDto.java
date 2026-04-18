package mars.tripplanappbackend.place.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 저장한 장소 카드에서 북마크를 다시 눌러 저장을 취소할 때 사용하는 요청 DTO입니다.
 * Controller에서 받은 경로 변수와 현재 로그인 사용자 정보를 하나로 묶어
 * Service 계층으로 전달하기 위한 용도로 사용합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "저장 취소 요청 DTO")
public class DeleteSavedPlaceRequestDto {

    @Schema(hidden = true)
    private Long placeId;

    @Schema(hidden = true)
    private String usersId;

    /**
     * 경로 변수 placeId와 현재 로그인 사용자 정보를 기준으로 저장 취소 요청 DTO를 생성합니다.
     *
     * @param placeId 저장 취소 대상 장소 PK
     * @param usersId 현재 로그인한 사용자 아이디
     * @return Service 계층에서 사용하는 저장 취소 요청 DTO
     */
    public static DeleteSavedPlaceRequestDto of(Long placeId, String usersId) {
        DeleteSavedPlaceRequestDto requestDto = new DeleteSavedPlaceRequestDto();
        requestDto.placeId = placeId;
        requestDto.usersId = usersId;
        return requestDto;
    }
}
