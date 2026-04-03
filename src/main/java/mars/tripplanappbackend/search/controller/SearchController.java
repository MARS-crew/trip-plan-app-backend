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

/**
 * 검색 페이지에서 사용하는 조회 및 삭제 API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "검색 관련 API")
public class SearchController {

    private final SearchService searchService;

    /**
     * 검색 페이지 상단에 고정 노출되는 카테고리 목록을 조회합니다.
     *
     * @return 공통 응답 형식으로 감싼 검색 카테고리 목록 응답
     */
    @GetMapping("/categories")
    @ApiErrorExceptions({ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "검색 카테고리 조회",
            description = "검색 페이지 상단에 고정 노출되는 6개의 검색 카테고리 목록을 조회합니다."
    )
    public ApiResponse<SearchCategoryListResponseDto> getSearchCategories() {
        SearchCategoryRequestDto requestDto = SearchCategoryRequestDto.create();
        return ApiResponse.ok(searchService.getSearchCategories(requestDto));
    }

    /**
     * 검색 완료 페이지에 노출할 검색 결과 리스트를 조회합니다.
     * 인증 사용자가 검색한 경우에는 최근 검색어 목록에도 검색어를 반영합니다.
     *
     * @param keyword 검색어
     * @param userPrincipal 커스텀 어노테이션으로 주입된 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 검색 결과 리스트 응답
     */
    @GetMapping("/results")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "검색 결과 리스트 조회",
            description = "검색 완료 페이지에 노출할 검색 결과 리스트와 검색 결과 개수를 조회합니다."
    )
    public ApiResponse<SearchResultListResponseDto> getSearchResults(
            @Parameter(description = "검색어", example = "삿포로")
            @RequestParam("keyword") String keyword,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        SearchResultListRequestDto requestDto = SearchResultListRequestDto.of(
                keyword,
                userPrincipal != null ? userPrincipal.getUsersId() : null
        );
        return ApiResponse.ok(searchService.getSearchResults(requestDto));
    }

    /**
     * 검색 페이지 하단에 노출할 인기 검색어 상위 5건을 조회합니다.
     *
     * @return 공통 응답 형식으로 감싼 인기 검색어 목록 응답
     */
    @GetMapping("/popular-searches")
    @ApiErrorExceptions({ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "인기 검색어 조회",
            description = "검색 페이지 하단에 노출할 인기 검색어 상위 5건을 조회합니다."
    )
    public ApiResponse<PopularSearchListResponseDto> getPopularSearches() {
        PopularSearchListRequestDto requestDto = PopularSearchListRequestDto.create();
        return ApiResponse.ok(searchService.getPopularSearches(requestDto));
    }

    /**
     * 검색 페이지에 노출할 최근 검색어 목록을 최신순으로 조회합니다.
     *
     * @param userPrincipal 커스텀 어노테이션으로 주입된 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 최근 검색어 목록 응답
     */
    @GetMapping("/recent-searches")
    @ApiErrorExceptions({ErrorCode.UNAUTHORIZED, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "최근 검색어 조회",
            description = "검색 페이지에 노출할 최근 검색어 최대 5건을 최신순으로 조회합니다."
    )
    public ApiResponse<RecentSearchListResponseDto> getRecentSearches(
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        RecentSearchListRequestDto requestDto = RecentSearchListRequestDto.of(userPrincipal.getUsersId());
        return ApiResponse.ok(searchService.getRecentSearches(requestDto));
    }

    /**
     * 검색 페이지 최근 검색어 목록에서 선택한 항목 한 건을 삭제합니다.
     *
     * @param recentSearchId 삭제할 최근 검색어 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입된 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 최근 검색어 삭제 응답
     */
    @DeleteMapping("/recent-searches/{recentSearchId}")
    @ApiErrorExceptions({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.INVALID_INPUT,
            ErrorCode.INTERNAL_ERROR
    })
    @Operation(
            summary = "최근 검색어 삭제",
            description = "검색 페이지 최근 검색어 목록에서 선택한 항목 한 건을 삭제합니다."
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

    /**
     * 검색 페이지 최근 검색어 목록에 남아 있는 항목을 전체 삭제합니다.
     *
     * @param userPrincipal 커스텀 어노테이션으로 주입된 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 최근 검색어 전체 삭제 응답
     */
    @DeleteMapping("/recent-searches")
    @ApiErrorExceptions({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.INTERNAL_ERROR
    })
    @Operation(
            summary = "최근 검색어 전체 삭제",
            description = "검색 페이지 최근 검색어 목록에 남아 있는 항목을 현재 로그인 사용자 기준으로 전체 삭제합니다."
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
