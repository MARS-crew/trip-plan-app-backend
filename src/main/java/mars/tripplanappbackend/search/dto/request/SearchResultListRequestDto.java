package mars.tripplanappbackend.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 검색 결과 목록 조회 요청 DTO입니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "검색 결과 목록 조회 요청 DTO")
public class SearchResultListRequestDto {

    @Schema(description = "검색어", example = "오사카")
    private String keyword;

    @Schema(description = "현재 로그인한 사용자 아이디", example = "cye4526", nullable = true)
    private String usersId;

    /**
     * 컨트롤러 입력값으로 검색 결과 목록 조회 요청 DTO를 생성합니다.
     *
     * @param keyword 검색어
     * @param usersId 현재 로그인한 사용자 아이디
     * @return 검색 결과 목록 조회 요청 DTO
     */
    public static SearchResultListRequestDto of(String keyword, String usersId) {
        SearchResultListRequestDto requestDto = new SearchResultListRequestDto();
        requestDto.keyword = keyword;
        requestDto.usersId = usersId;
        return requestDto;
    }
}
