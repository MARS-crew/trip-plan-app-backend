package mars.tripplanappbackend.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 현재 로그인한 사용자의 최근 검색어 전체 삭제 요청 DTO입니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "최근 검색어 전체 삭제 요청 DTO")
public class DeleteAllRecentSearchRequestDto {

    @Schema(description = "현재 로그인한 사용자 아이디", example = "cye4526")
    private String usersId;

    /**
     * 컨트롤러 입력값으로 최근 검색어 전체 삭제 요청 DTO를 생성합니다.
     *
     * @param usersId 현재 로그인한 사용자 아이디
     * @return 최근 검색어 전체 삭제 요청 DTO
     */
    public static DeleteAllRecentSearchRequestDto of(String usersId) {
        DeleteAllRecentSearchRequestDto requestDto = new DeleteAllRecentSearchRequestDto();
        requestDto.usersId = usersId;
        return requestDto;
    }
}
