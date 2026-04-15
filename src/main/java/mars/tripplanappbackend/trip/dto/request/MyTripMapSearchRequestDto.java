package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 내 여행지 상세의 지도 검색 결과 조회에 사용하는 서비스 요청 DTO입니다.
 * 특정 여행(tripId) 문맥에서 사용자가 입력한 검색어(keyword)를 기준으로
 * 지도 핀/리스트 카드에 필요한 장소 후보를 조회할 때 사용합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "내 여행지 상세 지도 검색 결과 조회 요청")
public class MyTripMapSearchRequestDto {

    @Schema(hidden = true)
    private Long tripId;

    @Schema(hidden = true)
    private String usersId;

    @Schema(hidden = true)
    private String keyword;

    /**
     * 경로 변수/쿼리 파라미터/로그인 사용자 정보를
     * 서비스 계층에서 사용하는 요청 DTO로 조합합니다.
     *
     * @param tripId 조회 대상 여행 PK
     * @param usersId 로그인 사용자 아이디
     * @param keyword 지도 검색어
     * @return 서비스 계층에서 사용할 지도 검색 요청 DTO
     */
    public static MyTripMapSearchRequestDto of(Long tripId, String usersId, String keyword) {
        MyTripMapSearchRequestDto requestDto = new MyTripMapSearchRequestDto();
        requestDto.tripId = tripId;
        requestDto.usersId = usersId;
        requestDto.keyword = keyword;
        return requestDto;
    }
}

