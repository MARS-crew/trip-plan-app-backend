package mars.tripplanappbackend.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 검색 페이지의 최근 검색어 목록 조회 요청 DTO입니다.
 * 현재 로그인한 사용자 기준으로 최근 검색어 5건을 조회할 때 사용합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "최근 검색어 목록 조회 요청 DTO")
public class RecentSearchListRequestDto {

    private static final int DEFAULT_LIMIT = 5;

    @Schema(description = "현재 로그인한 사용자 아이디", example = "cye4526")
    private String usersId;

    @Schema(description = "조회할 최근 검색어 최대 개수", example = "5")
    private int limit;

    /**
     * 컨트롤러 입력값으로 최근 검색어 목록 조회 요청 DTO를 생성합니다.
     *
     * @param usersId 현재 로그인한 사용자 아이디
     * @return 최근 검색어 목록 조회 요청 DTO
     */
    public static RecentSearchListRequestDto of(String usersId) {
        RecentSearchListRequestDto requestDto = new RecentSearchListRequestDto();
        requestDto.usersId = usersId;
        requestDto.limit = DEFAULT_LIMIT;
        return requestDto;
    }
}
