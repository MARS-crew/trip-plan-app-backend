package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 내 여행 상세 화면의 일정 리스트 조회에 사용하는 요청 DTO입니다.
 * 컨트롤러에서 받은 여행 PK와 현재 로그인 사용자의 아이디를 하나의 객체로 묶어서 서비스 계층에 전달합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "내 여행 일정 리스트 조회 요청")
public class MyTripScheduleListRequestDto {

    @Schema(hidden = true)
    private Long tripId;

    @Schema(hidden = true)
    private String usersId;

    /**
     * 컨트롤러에서 전달받은 경로 변수와 로그인 사용자 정보를 이용해 일정 리스트 조회 요청 DTO를 생성합니다.
     *
     * @param tripId 조회할 여행 PK
     * @param usersId 현재 로그인한 사용자 아이디
     * @return 서비스 계층에서 사용할 일정 리스트 조회 요청 DTO
     */
    public static MyTripScheduleListRequestDto of(Long tripId, String usersId) {
        MyTripScheduleListRequestDto requestDto = new MyTripScheduleListRequestDto();
        requestDto.tripId = tripId;
        requestDto.usersId = usersId;
        return requestDto;
    }
}
