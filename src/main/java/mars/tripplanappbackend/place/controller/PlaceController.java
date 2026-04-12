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
import mars.tripplanappbackend.place.dto.request.SavedPlaceListRequestDto;
import mars.tripplanappbackend.place.dto.request.SharePlaceRequestDto;
import mars.tripplanappbackend.place.dto.response.NearbyRecommendedPlaceListResponseDto;
import mars.tripplanappbackend.place.dto.response.PlaceDetailResponseDto;
import mars.tripplanappbackend.place.dto.response.RecommendedPlaceListResponseDto;
import mars.tripplanappbackend.place.dto.response.SavePlaceResponseDto;
import mars.tripplanappbackend.place.dto.response.SavedPlaceListResponseDto;
import mars.tripplanappbackend.place.dto.response.SharePlaceResponseDto;
import mars.tripplanappbackend.place.enums.SavedPlaceFilterType;
import mars.tripplanappbackend.place.service.PlaceService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides place APIs used by the home, saved place, and place detail screens.
 */
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
@Tag(name = "Place", description = "Place APIs")
public class PlaceController {

    private final PlaceService placeService;

    /**
     * Returns the recommended place list rendered on the home screen.
     *
     * @param requestDto recommended place query DTO
     * @return recommended place list wrapped with the common response format
     */
    @GetMapping("/recommended")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.UNAUTHORIZED, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "Get recommended places",
            description = "Returns the recommended place list rendered on the home screen."
    )
    public ApiResponse<RecommendedPlaceListResponseDto> getRecommendedPlaces(
            @Valid RecommendedPlaceRequestDto requestDto
    ) {
        return ApiResponse.ok(placeService.getRecommendedPlaces(requestDto));
    }

    /**
     * Returns the saved place list shown on the saved place page and trip add bottom sheet.
     *
     * @param filterType saved place filter type
     * @param userPrincipal authenticated user injected via the custom annotation
     * @return saved place list wrapped with the common response format
     */
    @GetMapping("/saved-places")
    @ApiErrorExceptions({ErrorCode.UNAUTHORIZED, ErrorCode.USER_NOT_FOUND, ErrorCode.INVALID_INPUT, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "Get saved places",
            description = "Returns the saved place list shown on the saved place page and trip add bottom sheet."
    )
    public ApiResponse<SavedPlaceListResponseDto> getSavedPlaces(
            @Parameter(description = "Saved place filter type", example = "ALL")
            @RequestParam(name = "filterType", defaultValue = "ALL") SavedPlaceFilterType filterType,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        SavedPlaceListRequestDto requestDto = SavedPlaceListRequestDto.of(userPrincipal.getUsersId(), filterType);
        return ApiResponse.ok(placeService.getSavedPlaces(requestDto));
    }

    /**
     * Saves the selected place from the place detail screen.
     *
     * @param placeId place PK to save
     * @param userPrincipal authenticated user injected via the custom annotation
     * @return save result wrapped with the common response format
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
            summary = "Save place",
            description = "Saves the selected place from the place detail screen."
    )
    public ApiResponse<SavePlaceResponseDto> savePlace(
            @Parameter(description = "Place PK to save", example = "7")
            @PathVariable("placeId") Long placeId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        SavePlaceRequestDto requestDto = SavePlaceRequestDto.of(placeId, userPrincipal.getUsersId());
        return ApiResponse.ok(placeService.savePlace(requestDto));
    }

    /**
     * Cancels a saved place when the bookmark is tapped again on the saved list or place detail screen.
     * The controller never accesses Authentication directly and only uses the injected UserPrincipal.
     *
     * @param placeId place PK to unsave
     * @param userPrincipal authenticated user injected via the custom annotation
     * @return unsave result wrapped with the common response format
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
            summary = "Unsave place",
            description = "Cancels a saved place when the bookmark is tapped again on the saved list or place detail screen."
    )
    public ApiResponse<SavePlaceResponseDto> deleteSavedPlace(
            @Parameter(description = "Place PK to unsave", example = "7")
            @PathVariable("placeId") Long placeId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        DeleteSavedPlaceRequestDto requestDto = DeleteSavedPlaceRequestDto.of(placeId, userPrincipal.getUsersId());
        return ApiResponse.ok(placeService.deleteSavedPlace(requestDto));
    }

    /**
     * Returns share metadata used by the place detail screen.
     *
     * @param placeId place PK to share
     * @param userPrincipal authenticated user injected via the custom annotation
     * @return share metadata wrapped with the common response format
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
            summary = "Share place",
            description = "Returns share metadata used by the place detail screen."
    )
    public ApiResponse<SharePlaceResponseDto> sharePlace(
            @Parameter(description = "Place PK to share", example = "7")
            @PathVariable("placeId") Long placeId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        SharePlaceRequestDto requestDto = SharePlaceRequestDto.of(placeId, userPrincipal.getUsersId());
        return ApiResponse.ok(placeService.sharePlace(requestDto));
    }

    /**
     * Returns nearby recommended places for the lower section of the place detail screen.
     *
     * @param placeId base place PK
     * @param userPrincipal authenticated user injected via the custom annotation
     * @return nearby recommended place list wrapped with the common response format
     */
    @GetMapping("/{placeId}/nearby-recommendations")
    @ApiErrorExceptions({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.INTERNAL_ERROR
    })
    @Operation(
            summary = "Get nearby recommended places",
            description = "Returns nearby recommended places for the lower section of the place detail screen."
    )
    public ApiResponse<NearbyRecommendedPlaceListResponseDto> getNearbyRecommendedPlaces(
            @Parameter(description = "Base place PK", example = "7")
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
     * Returns detailed place information required by the place detail screen.
     *
     * @param placeId place PK to load
     * @param userPrincipal authenticated user injected via the custom annotation
     * @return place detail wrapped with the common response format
     */
    @GetMapping("/{placeId}")
    @ApiErrorExceptions({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.INTERNAL_ERROR
    })
    @Operation(
            summary = "Get place detail",
            description = "Returns the base information, tags, saved state, and review previews for the place detail screen."
    )
    public ApiResponse<PlaceDetailResponseDto> findOne(
            @Parameter(description = "Place PK to load", example = "7")
            @PathVariable("placeId") Long placeId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return ApiResponse.ok(placeService.findOne(placeId, userPrincipal.getUsersId()));
    }
}
