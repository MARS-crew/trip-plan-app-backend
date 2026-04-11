package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 내 여행지 상세 화면에서 방문 기록 저장을 요청할 때 사용하는 DTO입니다.
 * 컨트롤러에서 전달받은 여행 PK, 로그인 사용자 아이디, 요청 본문을 하나로 묶어
 * 서비스 계층에서 일관된 방식으로 검증하고 처리할 수 있도록 사용합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "방문 기록 저장 요청 DTO")
public class AddVisitedPlaceRequestDto {

    @Schema(hidden = true)
    private Long tripId;

    @Schema(hidden = true)
    private String usersId;

    @NotNull(message = "방문 기록을 남길 장소 PK는 필수입니다.")
    @Schema(description = "방문 기록을 저장할 장소 PK", example = "7")
    private Long placeId;

    @NotNull(message = "방문 기록과 연결할 일정 PK는 필수입니다.")
    @Schema(description = "방문 기록과 연결할 일정 PK", example = "21")
    private Long tripScheduleId;

    /**
     * 경로 변수, 로그인 사용자 정보, 요청 본문을 서비스 전용 DTO로 조합합니다.
     *
     * @param tripId 방문 기록을 저장할 여행 PK
     * @param usersId 현재 로그인한 사용자 아이디
     * @param requestDto 컨트롤러에서 전달받은 요청 본문 DTO
     * @return 서비스 계층에서 사용할 방문 기록 저장 요청 DTO
     */
    public static AddVisitedPlaceRequestDto of(Long tripId, String usersId, AddVisitedPlaceRequestDto requestDto) {
        AddVisitedPlaceRequestDto serviceRequestDto = new AddVisitedPlaceRequestDto();
        serviceRequestDto.tripId = tripId;
        serviceRequestDto.usersId = usersId;
        serviceRequestDto.placeId = requestDto.placeId;
        serviceRequestDto.tripScheduleId = requestDto.tripScheduleId;
        return serviceRequestDto;
    }
}
