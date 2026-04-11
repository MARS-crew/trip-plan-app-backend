package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 내 여행 상세 화면에서 선택한 여행을 삭제할 때 사용하는 요청 DTO입니다.
 * 컨트롤러에서 받은 여행 PK와 현재 로그인 사용자 아이디를 한 객체로 묶어 서비스 계층으로 전달합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "내 여행 삭제 요청 DTO")
public class DeleteTripRequestDto {

    @Schema(hidden = true)
    private Long tripId;

    @Schema(hidden = true)
    private String usersId;

    /**
     * 컨트롤러에서 전달받은 경로 변수와 로그인 사용자 정보를 조합해 삭제 요청 DTO를 생성합니다.
     *
     * @param tripId 삭제할 여행 PK
     * @param usersId 현재 로그인한 사용자 아이디
     * @return 서비스 계층에서 사용할 여행 삭제 요청 DTO
     */
    public static DeleteTripRequestDto of(Long tripId, String usersId) {
        DeleteTripRequestDto requestDto = new DeleteTripRequestDto();
        requestDto.tripId = tripId;
        requestDto.usersId = usersId;
        return requestDto;
    }
}
