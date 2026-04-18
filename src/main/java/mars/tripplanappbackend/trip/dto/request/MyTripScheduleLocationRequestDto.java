package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 내 여행 일정 위치 조회 요청 DTO입니다.
 * 지도 보기 화면에서는 여행 PK와 현재 로그인한 사용자 아이디만 있으면
 * 일정별 위치, 핀 순서, 방문 인증 가능 여부를 모두 계산할 수 있으므로
 * 컨트롤러에서 받은 값을 하나의 요청 객체로 묶어서 서비스 계층에 전달합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "내 여행 일정 위치 조회 요청")
public class MyTripScheduleLocationRequestDto {

    @Schema(hidden = true)
    private Long tripId;

    @Schema(hidden = true)
    private String usersId;

    /**
     * 경로 변수의 여행 PK와 인증 사용자 아이디를 합쳐 지도용 일정 위치 조회 요청 DTO를 생성합니다.
     *
     * @param tripId 조회할 여행 PK
     * @param usersId 현재 로그인한 사용자 아이디
     * @return 서비스 계층에서 사용할 일정 위치 조회 요청 DTO
     */
    public static MyTripScheduleLocationRequestDto of(Long tripId, String usersId) {
        MyTripScheduleLocationRequestDto requestDto = new MyTripScheduleLocationRequestDto();
        requestDto.tripId = tripId;
        requestDto.usersId = usersId;
        return requestDto;
    }
}
