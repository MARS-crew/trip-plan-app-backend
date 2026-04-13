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
import mars.tripplanappbackend.place.dto.request.DeleteSavedPlaceRequestDto;
import mars.tripplanappbackend.place.dto.request.NearbyRecommendedPlaceRequestDto;
import mars.tripplanappbackend.place.dto.request.RecommendedPlaceRequestDto;
import mars.tripplanappbackend.place.dto.request.SavePlaceRequestDto;
import mars.tripplanappbackend.place.dto.request.SavedPlaceCategoryListRequestDto;
import mars.tripplanappbackend.place.dto.request.SavedPlaceListRequestDto;
import mars.tripplanappbackend.place.dto.request.SharePlaceRequestDto;
import mars.tripplanappbackend.place.dto.response.NearbyRecommendedPlaceListResponseDto;
import mars.tripplanappbackend.place.dto.response.PlaceDetailResponseDto;
import mars.tripplanappbackend.place.dto.response.RecommendedPlaceListResponseDto;
import mars.tripplanappbackend.place.dto.response.SavePlaceResponseDto;
import mars.tripplanappbackend.place.dto.response.SavedPlaceCategoryListResponseDto;
import mars.tripplanappbackend.place.dto.response.SavedPlaceListResponseDto;
import mars.tripplanappbackend.place.dto.response.SharePlaceResponseDto;
import mars.tripplanappbackend.place.service.PlaceService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메인 화면, 저장한 장소 화면, 장소 상세 화면에서 사용하는 장소 관련 API를 제공하는 Controller입니다.
 */
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
@Tag(name = "Place", description = "장소 관련 API")
public class PlaceController {

    private final PlaceService placeService;

    /**
     * 메인 화면에 노출할 추천 장소 목록을 조회합니다.
     *
     * @param requestDto 추천 장소 조회 요청 DTO
     * @return 공통 응답 형식으로 감싼 추천 장소 목록
     */
    @GetMapping("/recommended")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.UNAUTHORIZED, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "추천 장소 조회",
            description = "메인 화면에 노출할 추천 장소 목록을 조회합니다."
    )
    public ApiResponse<RecommendedPlaceListResponseDto> getRecommendedPlaces(
            @Valid RecommendedPlaceRequestDto requestDto
    ) {
        return ApiResponse.ok(placeService.getRecommendedPlaces(requestDto));
    }

    /**
     * 저장된 장소 목록을 조회합니다.
     * 화면에서는 `전체`, `관광지`, `맛집`, `해변`, `자연`, `랜드마크` 값을 사용할 수 있고,
     * API에서는 동일 의미의 영문 enum 문자열도 함께 허용합니다.
     *
     * @param filterType 저장된 장소 카테고리 값
     * @param userPrincipal 커스텀 어노테이션으로 주입된 인증 사용자 정보
     * @return 공통 응답 형식으로 감싼 저장된 장소 목록
     */
    @GetMapping("/saved-places")
    @ApiErrorExceptions({ErrorCode.UNAUTHORIZED, ErrorCode.USER_NOT_FOUND, ErrorCode.INVALID_INPUT, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "저장된 장소 조회",
            description = "저장한 장소 화면에서 선택한 카테고리값에 맞는 저장된 장소 목록을 조회합니다. 지원 값: 전체, 관광지, 맛집, 해변, 자연, 랜드마크 또는 ALL, ATTRACTION, RESTAURANT, BEACH, NATURE, LANDMARK"
    )
    public ApiResponse<SavedPlaceListResponseDto> getSavedPlaces(
            @Parameter(
                    description = "저장된 장소 카테고리 값. 한글 라벨과 영문 enum 값을 모두 지원합니다.",
                    example = "전체"
            )
            @RequestParam(name = "filterType", defaultValue = "ALL") String filterType,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        SavedPlaceListRequestDto requestDto = SavedPlaceListRequestDto.of(userPrincipal.getUsersId(), filterType);
        return ApiResponse.ok(placeService.getSavedPlaces(requestDto));
    }

    /**
     * 저장한 장소 카테고리 목록을 조회합니다.
     * 저장된 장소 조회 API와 분리하여, 카테고리 탭 렌더링에 필요한 카테고리 메타 정보를 제공합니다.
     *
     * @param userPrincipal 커스텀 어노테이션으로 주입된 인증 사용자 정보
     * @return 공통 응답 형식으로 감싼 저장한 장소 카테고리 목록
     */
    @GetMapping("/saved-places/categories")
    @ApiErrorExceptions({ErrorCode.UNAUTHORIZED, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "저장한 장소 카테고리 조회",
            description = "저장한 장소 화면 카테고리 탭에서 사용할 카테고리 코드, 라벨, 저장 개수를 조회합니다."
    )
    public ApiResponse<SavedPlaceCategoryListResponseDto> getSavedPlaceCategories(
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        SavedPlaceCategoryListRequestDto requestDto = SavedPlaceCategoryListRequestDto.of(userPrincipal.getUsersId());
        return ApiResponse.ok(placeService.getSavedPlaceCategories(requestDto));
    }

    /**
     * 장소 상세 화면에서 선택한 장소를 저장 목록에 추가합니다.
     *
     * @param placeId 저장할 장소 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입된 인증 사용자 정보
     * @return 공통 응답 형식으로 감싼 장소 저장 결과
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
            summary = "장소 저장",
            description = "장소 상세 화면에서 선택한 장소를 저장 목록에 추가합니다."
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

    /**
     * 저장한 장소 목록 화면 또는 장소 상세 화면에서 북마크를 다시 눌러 저장을 취소합니다.
     * Controller에서는 Authentication을 직접 다루지 않고, @CurrentUser로 주입된 사용자 정보만 사용합니다.
     *
     * @param placeId 저장 취소할 장소 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입된 인증 사용자 정보
     * @return 공통 응답 형식으로 감싼 장소 저장 취소 결과
     */
    @DeleteMapping("/{placeId}/saved-places")
    @ApiErrorExceptions({
            ErrorCode.INVALID_INPUT,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.SAVED_PLACE_NOT_FOUND,
            ErrorCode.INTERNAL_ERROR
    })
    @Operation(
            summary = "장소 저장 취소",
            description = "저장한 장소 목록 화면 또는 장소 상세 화면에서 북마크를 다시 눌러 저장을 취소합니다."
    )
    public ApiResponse<SavePlaceResponseDto> deleteSavedPlace(
            @Parameter(description = "저장 취소할 장소 PK", example = "7")
            @PathVariable("placeId") Long placeId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        DeleteSavedPlaceRequestDto requestDto = DeleteSavedPlaceRequestDto.of(placeId, userPrincipal.getUsersId());
        return ApiResponse.ok(placeService.deleteSavedPlace(requestDto));
    }

    /**
     * 장소 상세 화면에서 사용하는 공유 메타데이터를 조회합니다.
     *
     * @param placeId 공유할 장소 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입된 인증 사용자 정보
     * @return 공통 응답 형식으로 감싼 장소 공유 메타데이터
     */
    @GetMapping("/{placeId}/share")
    @ApiErrorExceptions({
            ErrorCode.INVALID_INPUT,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.INTERNAL_ERROR
    })
    @Operation(
            summary = "장소 공유",
            description = "장소 상세 화면에서 사용하는 공유 메타데이터를 조회합니다."
    )
    public ApiResponse<SharePlaceResponseDto> sharePlace(
            @Parameter(description = "공유할 장소 PK", example = "7")
            @PathVariable("placeId") Long placeId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        SharePlaceRequestDto requestDto = SharePlaceRequestDto.of(placeId, userPrincipal.getUsersId());
        return ApiResponse.ok(placeService.sharePlace(requestDto));
    }

    /**
     * 장소 상세 화면 하단에 노출할 주변 추천 장소 목록을 조회합니다.
     *
     * @param placeId 기준 장소 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입된 인증 사용자 정보
     * @return 공통 응답 형식으로 감싼 주변 추천 장소 목록
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
            description = "장소 상세 화면 하단에 노출할 주변 추천 장소 목록을 조회합니다."
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
     * 장소 상세 화면에 필요한 장소 기본 정보, 태그, 저장 여부, 리뷰 미리보기 정보를 조회합니다.
     *
     * @param placeId 조회할 장소 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입된 인증 사용자 정보
     * @return 공통 응답 형식으로 감싼 장소 상세 정보
     */
    @GetMapping("/{placeId}")
    @ApiErrorExceptions({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.INTERNAL_ERROR
    })
    @Operation(
            summary = "장소 상세 조회",
            description = "장소 상세 화면에 필요한 기본 정보, 태그, 저장 여부, 리뷰 미리보기 정보를 조회합니다."
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
