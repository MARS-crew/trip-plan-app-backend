package mars.tripplanappbackend.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 최근 검색어 전체 삭제 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "최근 검색어 전체 삭제 응답 DTO")
public class DeleteAllRecentSearchResponseDto {

    @Schema(description = "최근 검색어가 하나 이상 삭제되었는지 여부", example = "true")
    private boolean deletedAll;

    @Schema(description = "삭제된 최근 검색어 개수", example = "4")
    private int deletedCount;

    /**
     * 삭제된 최근 검색어 개수로 전체 삭제 응답 DTO를 생성합니다.
     *
     * @param deletedCount 삭제된 최근 검색어 개수
     * @return 최근 검색어 전체 삭제 응답 DTO
     */
    public static DeleteAllRecentSearchResponseDto from(int deletedCount) {
        return DeleteAllRecentSearchResponseDto.builder()
                .deletedAll(deletedCount > 0)
                .deletedCount(deletedCount)
                .build();
    }
}
