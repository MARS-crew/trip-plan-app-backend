package mars.tripplanappbackend.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.config.auth.CurrentUser;
import mars.tripplanappbackend.global.config.auth.UserPrincipal;
import mars.tripplanappbackend.global.config.swagger.ApiErrorExceptions;
import mars.tripplanappbackend.global.dto.ApiResponse;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.review.dto.response.ReviewPreviewResponseDto;
import mars.tripplanappbackend.review.service.ReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
