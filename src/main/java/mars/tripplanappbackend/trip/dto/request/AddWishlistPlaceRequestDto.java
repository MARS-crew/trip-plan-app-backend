package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 내 여행 상세 화면에서 위시리스트 장소를 추가할 때 사용하는 요청 DTO입니다.
 * 컨트롤러에서 전달받은 여행 PK, 로그인 사용자 아이디, 요청 본문 데이터를 하나로 묶어
 * 서비스 계층으로 전달하기 위해 사용합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "위시리스트 장소 추가 요청 DTO")
public class AddWishlistPlaceRequestDto {

    @Schema(hidden = true)
    private Long tripId;

    @Schema(hidden = true)
    private String usersId;

    @NotNull(message = "추가할 장소 PK는 필수입니다.")
    @Schema(description = "추가할 장소 PK", example = "7")
    private Long placeId;

    @NotNull(message = "선택한 날짜는 필수입니다.")
    @Schema(description = "위시리스트 추가를 진행하는 날짜 카드 기준 일정 날짜", example = "2026-04-20")
    private LocalDate scheduleDate;

    /**
     * 경로 변수, 로그인 사용자 정보, 요청 본문을 서비스 계층용 DTO로 조합합니다.
     *
     * @param tripId 장소를 추가할 여행 PK
     * @param usersId 현재 로그인한 사용자 아이디
     * @param requestDto 컨트롤러에서 전달받은 요청 본문 DTO
     * @return 서비스 계층에서 사용할 위시리스트 장소 추가 요청 DTO
     */
    public static AddWishlistPlaceRequestDto of(Long tripId, String usersId, AddWishlistPlaceRequestDto requestDto) {
        AddWishlistPlaceRequestDto serviceRequestDto = new AddWishlistPlaceRequestDto();
        serviceRequestDto.tripId = tripId;
        serviceRequestDto.usersId = usersId;
        serviceRequestDto.placeId = requestDto.placeId;
        serviceRequestDto.scheduleDate = requestDto.scheduleDate;
        return serviceRequestDto;
    }
}
