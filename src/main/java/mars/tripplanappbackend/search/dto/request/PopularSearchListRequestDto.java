package mars.tripplanappbackend.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인기 검색어 목록 조회 요청 DTO입니다.
 * 현재는 상위 5건 고정 조회를 위해 기본 limit 값만 관리합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "인기 검색어 목록 조회 요청 DTO")
public class PopularSearchListRequestDto {

    private static final int DEFAULT_LIMIT = 5;

    @Schema(description = "조회할 인기 검색어 최대 개수", example = "5")
    private int limit;

    /**
     * 컨트롤러에서 사용하는 기본 인기 검색어 조회 요청 DTO를 생성합니다.
     *
     * @return 인기 검색어 목록 조회 요청 DTO
     */
    public static PopularSearchListRequestDto create() {
        PopularSearchListRequestDto requestDto = new PopularSearchListRequestDto();
        requestDto.limit = DEFAULT_LIMIT;
        return requestDto;
    }
}
