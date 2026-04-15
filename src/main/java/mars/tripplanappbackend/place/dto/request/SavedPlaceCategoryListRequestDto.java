package mars.tripplanappbackend.place.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 저장한 장소 카테고리 목록 조회 요청 DTO입니다.
 * 인증 사용자 식별값만 전달하여 서비스에서 카테고리별 저장 개수를 계산합니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SavedPlaceCategoryListRequestDto {

    private String usersId;

    /**
     * 저장한 장소 카테고리 목록 조회 요청 DTO를 생성합니다.
     *
     * @param usersId 현재 로그인한 사용자 아이디
     * @return 저장한 장소 카테고리 목록 조회 요청 DTO
     */
    public static SavedPlaceCategoryListRequestDto of(String usersId) {
        return SavedPlaceCategoryListRequestDto.builder()
                .usersId(usersId)
                .build();
    }
}

