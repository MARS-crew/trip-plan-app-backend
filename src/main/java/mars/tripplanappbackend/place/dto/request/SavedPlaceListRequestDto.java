package mars.tripplanappbackend.place.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.place.enums.SavedPlaceFilterType;

/**
 * 저장한 장소 목록 조회에 사용하는 서비스 요청 DTO입니다.
 * 로그인 사용자 아이디와 필터 유형을 함께 묶어 서비스 계층에 전달합니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SavedPlaceListRequestDto {

    private String usersId;
    private SavedPlaceFilterType filterType;

    /**
     * 저장한 장소 목록 조회에 필요한 사용자 아이디와 필터 유형을 DTO로 생성합니다.
     *
     * @param usersId 현재 로그인한 사용자 아이디
     * @param filterType 저장한 장소 목록 필터 유형
     * @return 저장한 장소 목록 조회 요청 DTO
     */
    public static SavedPlaceListRequestDto of(String usersId, SavedPlaceFilterType filterType) {
        return SavedPlaceListRequestDto.builder()
                .usersId(usersId)
                .filterType(filterType == null ? SavedPlaceFilterType.ALL : filterType)
                .build();
    }
}
