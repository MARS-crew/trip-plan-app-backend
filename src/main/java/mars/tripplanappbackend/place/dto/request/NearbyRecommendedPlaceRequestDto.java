package mars.tripplanappbackend.place.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 여행지 상세 페이지의 주변 추천 장소를 조회하기 위한 요청 DTO입니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "주변 추천 장소 조회 요청 DTO")
public class NearbyRecommendedPlaceRequestDto {

    @Schema(description = "기준 장소 PK", example = "7")
    private Long placeId;

    @Schema(description = "인증 사용자 아이디", example = "cye4526")
    private String usersId;

    /**
     * 컨트롤러에서 전달받은 값으로 요청 DTO를 생성합니다.
     *
     * @param placeId 기준 장소 PK
     * @param usersId 인증 사용자 아이디
     * @return 주변 추천 장소 조회 요청 DTO
     */
    public static NearbyRecommendedPlaceRequestDto of(Long placeId, String usersId) {
        NearbyRecommendedPlaceRequestDto requestDto = new NearbyRecommendedPlaceRequestDto();
        requestDto.placeId = placeId;
        requestDto.usersId = usersId;
        return requestDto;
    }
}
