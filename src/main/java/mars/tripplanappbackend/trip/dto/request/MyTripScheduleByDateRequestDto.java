package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 내 여행 일정 날짜별 조회 요청 DTO입니다.
 * 여행 PK, 인증 사용자 아이디, 조회할 날짜를 함께 전달합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "내 여행 일정 날짜별 조회 요청")
public class MyTripScheduleByDateRequestDto {

    @Schema(description = "조회할 여행 PK", example = "7")
    private Long tripId;

    @Schema(description = "인증된 사용자 아이디", example = "cye4526")
    private String usersId;

    @Schema(description = "조회할 여행 날짜", example = "2026-02-15", nullable = true)
    private LocalDate targetDate;

    /**
     * 컨트롤러 입력값으로 내 여행 일정 날짜별 조회 요청 DTO를 생성합니다.
     *
     * @param tripId 조회할 여행 PK
     * @param usersId 인증된 사용자 아이디
     * @param targetDate 조회할 여행 날짜
     * @return 내 여행 일정 날짜별 조회 요청 DTO
     */
    public static MyTripScheduleByDateRequestDto of(Long tripId, String usersId, LocalDate targetDate) {
        MyTripScheduleByDateRequestDto requestDto = new MyTripScheduleByDateRequestDto();
        requestDto.tripId = tripId;
        requestDto.usersId = usersId;
        requestDto.targetDate = targetDate;
        return requestDto;
    }
}
