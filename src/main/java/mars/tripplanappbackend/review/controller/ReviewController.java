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
import mars.tripplanappbackend.review.service.ReviewService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 엔드포인트")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "리뷰 생성", description = "리뷰 생성 API")
    @ApiErrorExceptions({ErrorCode.USER_NOT_FOUND, ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.VISITED_PLACE_NOT_FOUND, ErrorCode.INVALID_INPUT, ErrorCode.INTERNAL_ERROR})
    public ApiResponse<ReviewCreateResponseDto> createReview(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody @Valid ReviewCreateRequestDto requestDto) {
        String usersId = userPrincipal.getUsersId();
        ReviewCreateResponseDto response = reviewService.createReview(usersId, requestDto);

        return ApiResponse.ok(response);
    }
}
