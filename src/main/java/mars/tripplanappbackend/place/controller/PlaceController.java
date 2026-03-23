package mars.tripplanappbackend.place.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.config.auth.CurrentUser;
import mars.tripplanappbackend.global.config.auth.UserPrincipal;
import mars.tripplanappbackend.global.config.swagger.ApiErrorExceptions;
import mars.tripplanappbackend.global.dto.ApiResponse;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.place.dto.request.RecommendedPlaceRequestDto;
import mars.tripplanappbackend.place.dto.request.SavePlaceRequestDto;
import mars.tripplanappbackend.place.dto.response.RecommendedPlaceListResponseDto;
import mars.tripplanappbackend.place.dto.response.SavePlaceResponseDto;
import mars.tripplanappbackend.place.service.PlaceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 장소 관련 조회 및 상세 화면 액션 API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
@Tag(name = "Place", description = "장소 엔드포인트")
public class PlaceController {

    private final PlaceService placeService;

    /**
     * 메인 페이지에 노출할 추천 여행지 목록을 조회합니다.
     *
     * @param requestDto 추천 여행지 조회 요청 DTO
     * @return 추천 여행지 목록 응답
     */
    @GetMapping("/recommended")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.UNAUTHORIZED, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "추천 여행지 조회",
            description = "메인 페이지에 노출할 추천 여행지 목록을 조회합니다."
    )
    public ApiResponse<RecommendedPlaceListResponseDto> getRecommendedPlaces(
            @Valid RecommendedPlaceRequestDto requestDto
    ) {
        return ApiResponse.ok(placeService.getRecommendedPlaces(requestDto));
    }

    /**
     * 여행지 상세 화면에서 선택한 장소를 저장 목록에 추가합니다.
     *
     * @param placeId 저장할 장소 PK
     * @param userPrincipal 현재 인증된 사용자 정보
     * @return 저장 항목 추가 응답
     */
    @PostMapping("/{placeId}/saved-places")
    @ApiErrorExceptions({
            ErrorCode.INVALID_INPUT,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.SAVED_PLACE_ALREADY_EXISTS,
            ErrorCode.INTERNAL_ERROR
    })
    @Operation(
            summary = "저장 항목 추가",
            description = "여행지 상세 페이지에서 선택한 장소를 저장 목록에 추가합니다."
    )
    public ApiResponse<SavePlaceResponseDto> savePlace(
            @Parameter(description = "저장할 장소 PK", example = "7")
            @PathVariable("placeId") Long placeId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        SavePlaceRequestDto requestDto = SavePlaceRequestDto.of(placeId, userPrincipal.getUsersId());
        return ApiResponse.ok(placeService.savePlace(requestDto));
    }
}
