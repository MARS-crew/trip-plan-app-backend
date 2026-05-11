package mars.tripplanappbackend.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.config.auth.CurrentUser;
import mars.tripplanappbackend.global.config.auth.UserPrincipal;
import mars.tripplanappbackend.global.config.swagger.ApiErrorExceptions;
import mars.tripplanappbackend.global.dto.ApiResponse;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.review.dto.request.ReviewCreateRequestDto;
import mars.tripplanappbackend.review.dto.response.ReviewCreateResponseDto;
import mars.tripplanappbackend.review.dto.response.ReviewInfoResponseDto;
import mars.tripplanappbackend.review.dto.response.ReviewPreviewResponseDto;
import mars.tripplanappbackend.review.service.ReviewService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 엔드포인트")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{visitedPlaceId}/info")
    @Operation(summary = "리뷰 작성 사전 조회",
            description = "리뷰 작성 화면 진입 시 방문 장소명, 방문 날짜 등 사전 정보를 조회합니다.")
    @ApiErrorExceptions({ErrorCode.FORBIDDEN, ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_REFRESH_TOKEN,
            ErrorCode.VISITED_PLACE_NOT_FOUND, ErrorCode.INVALID_INPUT, ErrorCode.INTERNAL_ERROR})
    public ApiResponse<ReviewPreviewResponseDto> getReviewPreview(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long visitedPlaceId
    ) {
        String usersId = userPrincipal.getUsersId();
        ReviewPreviewResponseDto response = reviewService.getReviewPreview(usersId, visitedPlaceId);
        return ApiResponse.ok(response);
    }

    @PostMapping
    @Operation(summary = "리뷰 작성",
            description = "별점, 내용, 사진으로 리뷰를 작성, 사진 없이 작성하고 싶다면 컬럼 삭제 || 빈 배열로 작성해 주세용")
    @ApiErrorExceptions({ErrorCode.FORBIDDEN, ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_REFRESH_TOKEN,
            ErrorCode.USER_NOT_FOUND, ErrorCode.VISITED_PLACE_NOT_FOUND,
            ErrorCode.DUPLICATE_REVIEW, ErrorCode.INVALID_INPUT, ErrorCode.INTERNAL_ERROR})
    public ApiResponse<ReviewCreateResponseDto> createReview(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody @Valid ReviewCreateRequestDto requestDto
    ) {
        String usersId = userPrincipal.getUsersId();
        ReviewCreateResponseDto response = reviewService.createReview(usersId, requestDto);
        return ApiResponse.ok(response);
    }

    @GetMapping("/place/{placeId}")
    @Operation(summary = "장소별 리뷰 목록 조회",
            description = "특정 장소의 평균 평점, 별점 분포 통계 및 리뷰 목록(최신순)을 조회합니다.")
    @ApiErrorExceptions({ErrorCode.PLACE_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    public ApiResponse<ReviewInfoResponseDto> getPlaceReviews(
            @PathVariable Long placeId
    ) {
        ReviewInfoResponseDto response = reviewService.getPlaceReviews(placeId);
        return ApiResponse.ok(response);
    }
}
