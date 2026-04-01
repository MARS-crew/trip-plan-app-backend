package mars.tripplanappbackend.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 인기 검색어 단건 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "인기 검색어 단건 응답")
public class PopularSearchResponseDto {

    @Schema(description = "인기 검색어 순위", example = "1")
    private int rank;

    @Schema(description = "검색 키워드", example = "삿포로 눈축제")
    private String keyword;

    @Schema(description = "해당 키워드 검색 횟수", example = "12")
    private long searchCount;

    /**
     * 순위와 집계 결과를 기반으로 인기 검색어 응답 DTO를 생성합니다.
     *
     * @param rank 인기 검색어 순위
     * @param keyword 검색 키워드
     * @param searchCount 해당 키워드 검색 횟수
     * @return 인기 검색어 단건 응답 DTO
     */
    public static PopularSearchResponseDto of(int rank, String keyword, long searchCount) {
        return PopularSearchResponseDto.builder()
                .rank(rank)
                .keyword(keyword)
                .searchCount(searchCount)
                .build();
    }
}
