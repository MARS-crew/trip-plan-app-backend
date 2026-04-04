package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 내 여행 전체 리스트 조회 요청 DTO입니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "내 여행 전체 리스트 조회 요청")
public class MyTripListRequestDto {

    @Schema(description = "인증된 사용자 아이디", example = "cye4526")
    private String usersId;

    /**
     * 인증 사용자 아이디로 요청 DTO를 생성합니다.
     *
     * @param usersId 인증된 사용자 아이디
     * @return 내 여행 전체 리스트 조회 요청 DTO
     */
    public static MyTripListRequestDto of(String usersId) {
        MyTripListRequestDto requestDto = new MyTripListRequestDto();
        requestDto.usersId = usersId;
        return requestDto;
    }
}
