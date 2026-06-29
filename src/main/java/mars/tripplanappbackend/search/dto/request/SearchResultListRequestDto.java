package mars.tripplanappbackend.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "Search result list request")
public class SearchResultListRequestDto {

    @Schema(description = "Search keyword", example = "삿포로")
    private String keyword;

    @Schema(description = "Current authenticated usersId", example = "cye4526", nullable = true)
    private String usersId;

    @Schema(description = "0-based page index", example = "0", defaultValue = "0")
    private Integer page;

    @Schema(description = "Page size", example = "20", defaultValue = "20")
    private Integer size;

    public static SearchResultListRequestDto of(String keyword, String usersId, Integer page, Integer size) {
        SearchResultListRequestDto requestDto = new SearchResultListRequestDto();
        requestDto.keyword = keyword;
        requestDto.usersId = usersId;
        requestDto.page = page;
        requestDto.size = size;
        return requestDto;
    }
}
