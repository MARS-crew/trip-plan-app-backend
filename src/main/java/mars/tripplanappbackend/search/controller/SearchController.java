package mars.tripplanappbackend.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.config.auth.CurrentUser;
import mars.tripplanappbackend.global.config.auth.UserPrincipal;
import mars.tripplanappbackend.global.config.swagger.ApiErrorExceptions;
import mars.tripplanappbackend.global.dto.ApiResponse;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.search.dto.request.DeleteAllRecentSearchRequestDto;
import mars.tripplanappbackend.search.dto.request.DeleteRecentSearchRequestDto;
import mars.tripplanappbackend.search.dto.request.PopularSearchListRequestDto;
import mars.tripplanappbackend.search.dto.request.RecentSearchListRequestDto;
import mars.tripplanappbackend.search.dto.request.SearchCategoryRequestDto;
import mars.tripplanappbackend.search.dto.request.SearchResultListRequestDto;
import mars.tripplanappbackend.search.dto.response.DeleteAllRecentSearchResponseDto;
import mars.tripplanappbackend.search.dto.response.DeleteRecentSearchResponseDto;
import mars.tripplanappbackend.search.dto.response.PopularSearchListResponseDto;
import mars.tripplanappbackend.search.dto.response.RecentSearchListResponseDto;
import mars.tripplanappbackend.search.dto.response.SearchCategoryListResponseDto;
import mars.tripplanappbackend.search.dto.response.SearchResultListResponseDto;
import mars.tripplanappbackend.search.service.SearchService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Search APIs")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/categories")
    @ApiErrorExceptions({ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "검색 카테고리 조회",
            description = "검색 화면 상단의 고정 카테고리 목록을 조회합니다."
    )
    public ApiResponse<SearchCategoryListResponseDto> getSearchCategories() {
        SearchCategoryRequestDto requestDto = SearchCategoryRequestDto.create();
        return ApiResponse.ok(searchService.getSearchCategories(requestDto));
    }

    @GetMapping("/results")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "검색 결과 조회",
            description = "검색어 기반 장소 검색 결과를 페이지 단위로 조회합니다."
    )
    public ApiResponse<SearchResultListResponseDto> getSearchResults(
            @Parameter(description = "검색어", example = "삿포로")
            @RequestParam("keyword") String keyword,
            @Parameter(description = "0-based page index", example = "0")
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "page size (max 20)", example = "20")
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        SearchResultListRequestDto requestDto = SearchResultListRequestDto.of(
                keyword,
                userPrincipal != null ? userPrincipal.getUsersId() : null,
                page,
                size
        );
        return ApiResponse.ok(searchService.getSearchResults(requestDto));
    }

    @GetMapping("/popular-searches")
    @ApiErrorExceptions({ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "인기 검색어 조회",
            description = "검색 화면 하단에 노출되는 인기 검색어 상위 5개를 조회합니다."
    )
    public ApiResponse<PopularSearchListResponseDto> getPopularSearches() {
        PopularSearchListRequestDto requestDto = PopularSearchListRequestDto.create();
        return ApiResponse.ok(searchService.getPopularSearches(requestDto));
    }

    @GetMapping("/recent-searches")
    @ApiErrorExceptions({ErrorCode.UNAUTHORIZED, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "최근 검색어 조회",
            description = "현재 로그인한 사용자의 최근 검색어 최대 5건을 최신순으로 조회합니다."
    )
    public ApiResponse<RecentSearchListResponseDto> getRecentSearches(
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        RecentSearchListRequestDto requestDto = RecentSearchListRequestDto.of(userPrincipal.getUsersId());
        return ApiResponse.ok(searchService.getRecentSearches(requestDto));
    }

    @DeleteMapping("/recent-searches/{recentSearchId}")
    @ApiErrorExceptions({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.INVALID_INPUT,
            ErrorCode.INTERNAL_ERROR
    })
    @Operation(
            summary = "최근 검색어 삭제",
            description = "최근 검색어 목록에서 선택한 항목 1건을 삭제합니다."
    )
    public ApiResponse<DeleteRecentSearchResponseDto> deleteRecentSearch(
            @Parameter(description = "삭제할 최근 검색어 PK", example = "12")
            @PathVariable("recentSearchId") Long recentSearchId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        DeleteRecentSearchRequestDto requestDto = DeleteRecentSearchRequestDto.of(
                recentSearchId,
                userPrincipal.getUsersId()
        );
        return ApiResponse.ok(searchService.deleteRecentSearch(requestDto));
    }

    @DeleteMapping("/recent-searches")
    @ApiErrorExceptions({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.INTERNAL_ERROR
    })
    @Operation(
            summary = "최근 검색어 전체 삭제",
            description = "현재 로그인한 사용자의 최근 검색어 목록 전체를 삭제합니다."
    )
    public ApiResponse<DeleteAllRecentSearchResponseDto> deleteAllRecentSearches(
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        DeleteAllRecentSearchRequestDto requestDto =
                DeleteAllRecentSearchRequestDto.of(userPrincipal.getUsersId());
        return ApiResponse.ok(searchService.deleteAllRecentSearches(requestDto));
    }
}
