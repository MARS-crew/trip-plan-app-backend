package mars.tripplanappbackend.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 검색 페이지의 최근 검색어 목록 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "최근 검색어 목록 응답")
public class RecentSearchListResponseDto {

    @Schema(description = "최근 검색어 개수", example = "4")
    private int searchCount;

    @Schema(description = "최근 검색어 목록")
    private List<RecentSearchResponseDto> recentSearches;

    /**
     * 최근 검색어 목록으로 목록 응답 DTO를 생성합니다.
     *
     * @param recentSearches 최근 검색어 목록
     * @return 최근 검색어 목록 응답 DTO
     */
    public static RecentSearchListResponseDto of(List<RecentSearchResponseDto> recentSearches) {
        return RecentSearchListResponseDto.builder()
                .searchCount(recentSearches.size())
                .recentSearches(recentSearches)
                .build();
    }
}
