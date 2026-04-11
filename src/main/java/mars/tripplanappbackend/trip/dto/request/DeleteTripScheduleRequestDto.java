package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 내 여행 상세 화면의 일정 리스트에서 개별 일정을 삭제할 때 사용하는 요청 DTO입니다.
 * 컨트롤러에서 전달받은 여행 PK, 일정 PK, 로그인 사용자 아이디를 하나의 객체로 묶어 서비스 계층에 전달합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "개별 일정 삭제 요청")
public class DeleteTripScheduleRequestDto {

    @Schema(hidden = true)
    private Long tripId;

    @Schema(hidden = true)
    private Long tripScheduleId;

    @Schema(hidden = true)
    private String usersId;

    /**
     * 경로 변수와 로그인 사용자 정보를 기반으로 개별 일정 삭제 요청 DTO를 생성합니다.
     *
     * @param tripId 일정이 속한 여행 PK
     * @param tripScheduleId 삭제할 일정 PK
     * @param usersId 현재 로그인한 사용자 아이디
     * @return 서비스 계층에서 사용할 개별 일정 삭제 요청 DTO
     */
    public static DeleteTripScheduleRequestDto of(Long tripId, Long tripScheduleId, String usersId) {
        DeleteTripScheduleRequestDto requestDto = new DeleteTripScheduleRequestDto();
        requestDto.tripId = tripId;
        requestDto.tripScheduleId = tripScheduleId;
        requestDto.usersId = usersId;
        return requestDto;
    }
}
