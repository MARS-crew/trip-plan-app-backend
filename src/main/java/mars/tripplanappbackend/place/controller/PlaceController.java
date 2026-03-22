package mars.tripplanappbackend.place.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.config.swagger.ApiErrorExceptions;
import mars.tripplanappbackend.global.dto.ApiResponse;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.place.dto.response.PlaceDetailResponseDto;
import mars.tripplanappbackend.place.service.PlaceService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 장소 상세 페이지에서 사용하는 조회 API
 */
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
@Tag(name = "Place", description = "장소 도메인")
public class PlaceController {

    private final PlaceService placeService;

    /**
     * 여행지 상세 페이지에 필요한 장소 상세 정보를 조회합니다.
     *
     * @param placeId 조회할 장소 PK
     * @param usersId JWT 인증 정보에서 추출한 사용자 아이디
     * @return 장소 기본 정보, 태그, 저장 여부, 리뷰 미리보기를 포함한 공통 응답
     */
    @GetMapping("/{placeId}")
    @ApiErrorExceptions({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.INTERNAL_ERROR
    })
    @Operation(
            summary = "여행지 상세 조회",
            description = "여행지 상세 페이지에 필요한 장소 기본 정보, 태그, 저장 여부, 리뷰 미리보기를 조회합니다."
    )
    public ApiResponse<PlaceDetailResponseDto> findOne(
            @Parameter(description = "조회할 장소 PK", example = "7")
            @PathVariable("placeId") Long placeId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal String usersId
    ) {
        return ApiResponse.ok(placeService.findOne(placeId, usersId));
    }
}
