package mars.tripplanappbackend.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 검색 카테고리 목록 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "검색 카테고리 목록 응답")
public class SearchCategoryListResponseDto {

    @Schema(description = "검색 카테고리 개수", example = "6")
    private int categoryCount;

    @Schema(description = "검색 카테고리 목록")
    private List<SearchCategoryResponseDto> categories;

    /**
     * 검색 카테고리 목록으로 목록 응답 DTO를 생성합니다.
     *
     * @param categories 검색 카테고리 목록
     * @return 검색 카테고리 목록 응답 DTO
     */
    public static SearchCategoryListResponseDto of(List<SearchCategoryResponseDto> categories) {
        return SearchCategoryListResponseDto.builder()
                .categoryCount(categories.size())
                .categories(categories)
                .build();
    }
}
