package mars.tripplanappbackend.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.search.enums.SearchCategory;

/**
 * 검색 카테고리 단건 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "검색 카테고리 단건 응답")
public class SearchCategoryResponseDto {

    @Schema(description = "카테고리 코드", example = "ACCOMMODATION")
    private String categoryCode;

    @Schema(description = "카테고리명", example = "숙박")
    private String categoryName;

    @Schema(description = "카테고리 노출 순서", example = "1")
    private int sortOrder;

    /**
     * 검색 카테고리 enum을 응답 DTO로 변환합니다.
     *
     * @param searchCategory 검색 카테고리 enum
     * @return 검색 카테고리 응답 DTO
     */
    public static SearchCategoryResponseDto from(SearchCategory searchCategory) {
        return SearchCategoryResponseDto.builder()
                .categoryCode(searchCategory.getCode())
                .categoryName(searchCategory.getName())
                .sortOrder(searchCategory.getSortOrder())
                .build();
    }
}
