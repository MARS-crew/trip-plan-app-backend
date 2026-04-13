package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 내 여행 상세 화면의 "길찾기" 액션에서 사용하는 서비스 요청 DTO입니다.
 * 특정 여행의 특정 일정(tripScheduleId)을 기준으로 목적지 정보를 검증해야 하므로
 * 여행 PK, 일정 PK, 로그인 사용자 아이디를 하나의 요청 객체로 묶어 서비스 계층에 전달합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "내 여행 상세 길찾기 요청")
public class MyTripScheduleRouteRequestDto {

    @Schema(hidden = true)
    private Long tripId;

    @Schema(hidden = true)
    private Long tripScheduleId;

    @Schema(hidden = true)
    private String usersId;

    /**
     * 여행 PK, 일정 PK, 로그인 사용자 아이디를 조합해 길찾기 요청 DTO를 생성합니다.
     *
     * @param tripId 조회할 여행 PK
     * @param tripScheduleId 길찾기 대상 일정 PK
     * @param usersId 로그인 사용자 아이디
     * @return 서비스 계층에서 사용할 길찾기 요청 DTO
     */
    public static MyTripScheduleRouteRequestDto of(Long tripId, Long tripScheduleId, String usersId) {
        MyTripScheduleRouteRequestDto requestDto = new MyTripScheduleRouteRequestDto();
        requestDto.tripId = tripId;
        requestDto.tripScheduleId = tripScheduleId;
        requestDto.usersId = usersId;
        return requestDto;
    }
}
