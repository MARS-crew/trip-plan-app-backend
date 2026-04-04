package mars.tripplanappbackend.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 검색 결과 리스트 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "검색 결과 리스트 응답")
public class SearchResultListResponseDto {

    @Schema(description = "검색어", example = "삿포로")
    private String keyword;

    @Schema(description = "검색 결과 개수", example = "1")
    private int resultCount;

    @Schema(description = "검색 결과 목록")
    private List<SearchResultResponseDto> searchResults;

    /**
     * 검색어와 검색 결과 목록으로 검색 결과 리스트 응답 DTO를 생성합니다.
     *
     * @param keyword 검색어
     * @param searchResults 검색 결과 목록
     * @return 검색 결과 리스트 응답 DTO
     */
    public static SearchResultListResponseDto of(String keyword, List<SearchResultResponseDto> searchResults) {
        return SearchResultListResponseDto.builder()
                .keyword(keyword)
                .resultCount(searchResults.size())
                .searchResults(searchResults)
                .build();
    }
}
