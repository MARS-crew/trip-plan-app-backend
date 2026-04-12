package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 내 여행 상세 화면 전체를 조회할 때 사용하는 서비스 요청 DTO입니다.
 * Controller에서 받은 여행 PK와 로그인 사용자 아이디를 한 객체로 묶어 Service 계층에 전달합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "내 여행 상세 조회 요청")
public class MyTripDetailRequestDto {

    @Schema(hidden = true)
    private Long tripId;

    @Schema(hidden = true)
    private String usersId;

    /**
     * 경로 변수의 여행 PK와 현재 로그인 사용자 아이디를 묶어 상세 조회 요청 DTO를 생성합니다.
     *
     * @param tripId 조회할 여행 PK
     * @param usersId 현재 로그인한 사용자 아이디
     * @return Service 계층에서 사용할 내 여행 상세 조회 요청 DTO
     */
    public static MyTripDetailRequestDto of(Long tripId, String usersId) {
        MyTripDetailRequestDto requestDto = new MyTripDetailRequestDto();
        requestDto.tripId = tripId;
        requestDto.usersId = usersId;
        return requestDto;
    }
}
