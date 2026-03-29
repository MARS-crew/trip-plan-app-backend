package mars.tripplanappbackend.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.config.swagger.ApiErrorExceptions;
import mars.tripplanappbackend.global.dto.ApiResponse;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.search.dto.request.SearchCategoryRequestDto;
import mars.tripplanappbackend.search.dto.response.SearchCategoryListResponseDto;
import mars.tripplanappbackend.search.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 검색 페이지에서 사용하는 조회 API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "검색 엔드포인트")
public class SearchController {

    private final SearchService searchService;

    /**
     * 검색 페이지 상단에 노출할 고정 카테고리 목록을 조회합니다.
     *
     * @return 공통 응답 형식으로 감싼 검색 카테고리 목록 응답
     */
    @GetMapping("/categories")
    @ApiErrorExceptions({ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "검색 카테고리 조회",
            description = "검색 페이지에 고정 노출되는 6개의 검색 카테고리 목록을 조회합니다."
    )
    public ApiResponse<SearchCategoryListResponseDto> getSearchCategories() {
        SearchCategoryRequestDto requestDto = SearchCategoryRequestDto.create();
        return ApiResponse.ok(searchService.getSearchCategories(requestDto));
    }
}
