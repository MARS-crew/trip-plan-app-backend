package mars.tripplanappbackend.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 최근 검색어 삭제 요청 DTO입니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "최근 검색어 삭제 요청 DTO")
public class DeleteRecentSearchRequestDto {

    @Schema(description = "삭제할 최근 검색어 PK", example = "12")
    private Long recentSearchId;

    @Schema(description = "현재 로그인한 사용자 아이디", example = "cye4526")
    private String usersId;

    /**
     * 컨트롤러 입력값으로 최근 검색어 삭제 요청 DTO를 생성합니다.
     *
     * @param recentSearchId 삭제할 최근 검색어 PK
     * @param usersId 현재 로그인한 사용자 아이디
     * @return 최근 검색어 삭제 요청 DTO
     */
    public static DeleteRecentSearchRequestDto of(Long recentSearchId, String usersId) {
        DeleteRecentSearchRequestDto requestDto = new DeleteRecentSearchRequestDto();
        requestDto.recentSearchId = recentSearchId;
        requestDto.usersId = usersId;
        return requestDto;
    }
}
