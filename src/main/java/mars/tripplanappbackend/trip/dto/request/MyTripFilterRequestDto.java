package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.trip.enums.MyTripFilterType;

/**
 * 내 여행 페이지 필터별 조회 요청 DTO입니다.
 * 로그인 사용자 아이디와 선택한 필터 탭 정보를 함께 전달합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "내 여행 필터별 조회 요청")
public class MyTripFilterRequestDto {

    @Schema(description = "인증된 사용자 아이디", example = "cye4526")
    private String usersId;

    @Schema(description = "내 여행 필터 유형", example = "UPCOMING")
    private MyTripFilterType filterType;

    /**
     * 컨트롤러 입력값으로 내 여행 필터별 조회 요청 DTO를 생성합니다.
     *
     * @param usersId 인증된 사용자 아이디
     * @param filterType 화면에서 선택한 내 여행 필터 유형
     * @return 내 여행 필터별 조회 요청 DTO
     */
    public static MyTripFilterRequestDto of(String usersId, MyTripFilterType filterType) {
        MyTripFilterRequestDto requestDto = new MyTripFilterRequestDto();
        requestDto.usersId = usersId;
        requestDto.filterType = filterType;
        return requestDto;
    }
}
