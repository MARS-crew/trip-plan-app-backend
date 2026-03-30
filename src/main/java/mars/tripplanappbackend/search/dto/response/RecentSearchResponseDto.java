package mars.tripplanappbackend.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.search.domain.RecentSearch;

/**
 * 최근 검색어 단건 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "최근 검색어 단건 응답")
public class RecentSearchResponseDto {

    @Schema(description = "최근 검색어 PK", example = "12")
    private Long recentSearchId;

    @Schema(description = "검색 키워드", example = "제주도 맛집")
    private String keyword;

    /**
     * 최근 검색어 엔티티를 응답 DTO로 변환합니다.
     *
     * @param recentSearch 최근 검색어 엔티티
     * @return 최근 검색어 단건 응답 DTO
     */
    public static RecentSearchResponseDto from(RecentSearch recentSearch) {
        return RecentSearchResponseDto.builder()
                .recentSearchId(recentSearch.getRecentSearchId())
                .keyword(recentSearch.getKeyword())
                .build();
    }
}
