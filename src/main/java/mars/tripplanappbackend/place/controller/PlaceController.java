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
import mars.tripplanappbackend.place.dto.request.NearbyRecommendedPlaceRequestDto;
import mars.tripplanappbackend.place.dto.request.RecommendedPlaceRequestDto;
import mars.tripplanappbackend.place.dto.response.NearbyRecommendedPlaceListResponseDto;
import mars.tripplanappbackend.place.dto.response.PlaceDetailResponseDto;
import mars.tripplanappbackend.place.dto.response.RecommendedPlaceListResponseDto;
import mars.tripplanappbackend.place.service.PlaceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메인 페이지와 여행지 상세 페이지에서 사용하는 장소 조회 API를 제공하는 컨트롤러입니다.
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
     * @return 공통 응답 형식으로 감싼 추천 여행지 목록 응답
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
     * 여행지 상세 페이지 하단에 노출할 주변 추천 장소 목록을 조회합니다.
     *
     * @param placeId 기준 장소 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입된 인증 사용자 정보
     * @return 공통 응답 형식으로 감싼 주변 추천 장소 목록 응답
     */
    @GetMapping("/{placeId}/nearby-recommendations")
    @ApiErrorExceptions({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.INTERNAL_ERROR
    })
    @Operation(
            summary = "주변 추천 장소 조회",
            description = "여행지 상세 페이지 하단에 노출할 주변 추천 장소 목록을 조회합니다."
    )
    public ApiResponse<NearbyRecommendedPlaceListResponseDto> getNearbyRecommendedPlaces(
            @Parameter(description = "기준 장소 PK", example = "7")
            @PathVariable("placeId") Long placeId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        NearbyRecommendedPlaceRequestDto requestDto = NearbyRecommendedPlaceRequestDto.of(
                placeId,
                userPrincipal.getUsersId()
        );

        return ApiResponse.ok(placeService.getNearbyRecommendedPlaces(requestDto));
    }

    /**
     * 여행지 상세 페이지에 필요한 장소 상세 정보를 조회합니다.
     *
     * @param placeId 조회할 장소 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입된 인증 사용자 정보
     * @return 공통 응답 형식으로 감싼 여행지 상세 응답
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
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return ApiResponse.ok(placeService.findOne(placeId, userPrincipal.getUsersId()));
    }
}
