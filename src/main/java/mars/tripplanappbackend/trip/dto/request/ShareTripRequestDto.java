package mars.tripplanappbackend.trip.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 내 여행 상세 화면 공유 시트에 필요한 정보를 조회할 때 사용하는 서비스 요청 DTO입니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ShareTripRequestDto {

    private Long tripId;
    private String usersId;

    /**
     * 여행 PK와 로그인 사용자 아이디를 서비스 요청 DTO로 변환합니다.
     *
     * @param tripId 공유할 여행 PK
     * @param usersId 공유를 요청한 사용자 아이디
     * @return 여행 공유 요청 DTO
     */
    public static ShareTripRequestDto of(Long tripId, String usersId) {
        return ShareTripRequestDto.builder()
                .tripId(tripId)
                .usersId(usersId)
                .build();
    }
}
