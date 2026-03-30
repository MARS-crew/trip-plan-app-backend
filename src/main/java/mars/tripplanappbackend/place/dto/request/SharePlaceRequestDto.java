package mars.tripplanappbackend.place.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 여행지 상세 화면에서 공유 정보를 조회할 때 사용하는 서비스 요청 DTO입니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SharePlaceRequestDto {

    private Long placeId;
    private String usersId;

    /**
     * 장소 PK와 사용자 아이디를 공유 요청 DTO로 변환합니다.
     *
     * @param placeId 공유할 장소 PK
     * @param usersId 공유를 요청한 사용자 아이디
     * @return 공유 요청 DTO
     */
    public static SharePlaceRequestDto of(Long placeId, String usersId) {
        return SharePlaceRequestDto.builder()
                .placeId(placeId)
                .usersId(usersId)
                .build();
    }
}
