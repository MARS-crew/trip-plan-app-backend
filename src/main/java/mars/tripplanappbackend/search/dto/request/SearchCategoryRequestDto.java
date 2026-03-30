package mars.tripplanappbackend.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 검색 카테고리 조회 요청 DTO입니다.
 * 현재는 별도 파라미터가 없지만, 서비스 시그니처를 통일하기 위해 사용합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "검색 카테고리 조회 요청 DTO")
public class SearchCategoryRequestDto {

    /**
     * 컨트롤러에서 사용할 기본 요청 DTO를 생성합니다.
     *
     * @return 검색 카테고리 조회 요청 DTO
     */
    public static SearchCategoryRequestDto create() {
        return new SearchCategoryRequestDto();
    }
}
