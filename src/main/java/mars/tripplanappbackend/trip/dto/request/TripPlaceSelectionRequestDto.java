package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 내 여행 상세 화면의 장소 추가하기 버튼을 눌렀을 때 표시할
 * 저장한 장소/위시리스트 목록 조회 요청 DTO입니다.
 */
@Schema(description = "저장한 장소/위시리스트 조회 요청 DTO")
public class TripPlaceSelectionRequestDto {

    @Schema(hidden = true)
    private Long tripId;

    @Schema(hidden = true)
    private String usersId;

    private TripPlaceSelectionRequestDto() {
    }

    public Long getTripId() {
        return tripId;
    }

    public String getUsersId() {
        return usersId;
    }

    /**
     * 경로 변수와 로그인 사용자 정보를 서비스 계층용 DTO로 조합합니다.
     *
     * @param tripId 조회 대상 여행 PK
     * @param usersId 현재 로그인한 사용자 아이디
     * @return 서비스 계층에서 사용할 조회 요청 DTO
     */
    public static TripPlaceSelectionRequestDto of(Long tripId, String usersId) {
        TripPlaceSelectionRequestDto requestDto = new TripPlaceSelectionRequestDto();
        requestDto.tripId = tripId;
        requestDto.usersId = usersId;
        return requestDto;
    }
}
