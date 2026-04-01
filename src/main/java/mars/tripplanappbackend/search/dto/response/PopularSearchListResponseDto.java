package mars.tripplanappbackend.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 인기 검색어 목록 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "인기 검색어 목록 응답")
public class PopularSearchListResponseDto {

    @Schema(description = "인기 검색어 목록 개수", example = "5")
    private int popularSearchCount;

    @Schema(description = "인기 검색어 목록")
    private List<PopularSearchResponseDto> popularSearches;

    /**
     * 인기 검색어 목록으로 목록 응답 DTO를 생성합니다.
     *
     * @param popularSearches 인기 검색어 목록
     * @return 인기 검색어 목록 응답 DTO
     */
    public static PopularSearchListResponseDto of(List<PopularSearchResponseDto> popularSearches) {
        return PopularSearchListResponseDto.builder()
                .popularSearchCount(popularSearches.size())
                .popularSearches(popularSearches)
                .build();
    }
}
