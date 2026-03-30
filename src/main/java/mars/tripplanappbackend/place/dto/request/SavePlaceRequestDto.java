package mars.tripplanappbackend.place.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 여행지 상세 화면에서 저장 항목 추가에 사용하는 서비스 요청 DTO입니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SavePlaceRequestDto {

    private Long placeId;
    private String usersId;

    /**
     * 장소 PK와 사용자 아이디를 저장 요청 DTO로 변환합니다.
     *
     * @param placeId 저장할 장소 PK
     * @param usersId 저장을 요청한 사용자 아이디
     * @return 저장 요청 DTO
     */
    public static SavePlaceRequestDto of(Long placeId, String usersId) {
        return SavePlaceRequestDto.builder()
                .placeId(placeId)
                .usersId(usersId)
                .build();
    }
}
