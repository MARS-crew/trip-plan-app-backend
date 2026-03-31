package mars.tripplanappbackend.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.search.domain.RecentSearch;

/**
 * 최근 검색어 삭제 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "최근 검색어 삭제 응답")
public class DeleteRecentSearchResponseDto {

    @Schema(description = "삭제된 최근 검색어 PK", example = "12")
    private Long recentSearchId;

    @Schema(description = "삭제 처리 여부", example = "true")
    private boolean deleted;

    /**
     * 삭제 처리된 최근 검색어 엔티티로 삭제 응답 DTO를 생성합니다.
     *
     * @param recentSearch 삭제 처리된 최근 검색어 엔티티
     * @return 최근 검색어 삭제 응답 DTO
     */
    public static DeleteRecentSearchResponseDto from(RecentSearch recentSearch) {
        return DeleteRecentSearchResponseDto.builder()
                .recentSearchId(recentSearch.getRecentSearchId())
                .deleted(Boolean.TRUE.equals(recentSearch.getIsDeleted()))
                .build();
    }
}
