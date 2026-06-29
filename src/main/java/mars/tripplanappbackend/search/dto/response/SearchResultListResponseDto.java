package mars.tripplanappbackend.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "Search result list response")
public class SearchResultListResponseDto {

    @Schema(description = "Search keyword", example = "삿포로")
    private String keyword;

    @Schema(description = "Number of results in the current page", example = "20")
    private int resultCount;

    @Schema(description = "Total number of fetched results for the keyword", example = "37")
    private int totalCount;

    @Schema(description = "0-based page index", example = "0")
    private int page;

    @Schema(description = "Page size", example = "20")
    private int size;

    @Schema(description = "Whether there is a next page", example = "true")
    private boolean hasNext;

    @Schema(description = "Next page index, null when there is no next page", example = "1", nullable = true)
    private Integer nextPage;

    @Schema(description = "Search result items")
    private List<SearchResultResponseDto> searchResults;

    public static SearchResultListResponseDto of(
            String keyword,
            int page,
            int size,
            int totalCount,
            List<SearchResultResponseDto> searchResults
    ) {
        boolean hasNext = (page + 1) * size < totalCount;

        return SearchResultListResponseDto.builder()
                .keyword(keyword)
                .resultCount(searchResults.size())
                .totalCount(totalCount)
                .page(page)
                .size(size)
                .hasNext(hasNext)
                .nextPage(hasNext ? page + 1 : null)
                .searchResults(searchResults)
                .build();
    }
}
