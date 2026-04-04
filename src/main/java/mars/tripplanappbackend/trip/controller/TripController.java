package mars.tripplanappbackend.trip.controller;

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
import mars.tripplanappbackend.trip.dto.request.MyTripFilterRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripListRequestDto;
import mars.tripplanappbackend.trip.dto.request.NearbyTripScheduleRequestDto;
import mars.tripplanappbackend.trip.dto.response.MyTripListResponseDto;
import mars.tripplanappbackend.trip.dto.response.NearbyTripScheduleResponseDto;
import mars.tripplanappbackend.trip.enums.MyTripFilterType;
import mars.tripplanappbackend.trip.service.TripService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메인 페이지와 내 여행 페이지에서 사용하는 여행 조회 API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Tag(name = "Trip", description = "여행 관련 API")
public class TripController {

    private final TripService tripService;

    /**
     * 내 여행 페이지 전체 탭에서 사용하는 여행 카드 목록을 조회합니다.
     *
     * @param userPrincipal 커스텀 어노테이션으로 주입된 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 전체 여행 카드 목록 응답
     */
    @GetMapping
    @ApiErrorExceptions({ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "내 여행 전체 리스트 조회",
            description = "내 여행 페이지 전체 탭에 노출되는 여행 카드 목록을 상태값과 함께 조회합니다."
    )
    public ApiResponse<MyTripListResponseDto> getMyTrips(
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        MyTripListRequestDto requestDto = MyTripListRequestDto.of(userPrincipal.getUsersId());
        return ApiResponse.ok(tripService.getMyTrips(requestDto));
    }

    /**
     * 내 여행 페이지 상단 필터 탭에서 선택한 유형에 따라 여행 카드 목록을 조회합니다.
     * 전체 탭은 모든 여행을 반환하고, 예정된 여행 탭은 여행 예정과 여행 중 상태를 함께 반환하며,
     * 지난 여행 탭은 여행 종료 상태의 카드만 반환합니다.
     *
     * @param filterType 화면에서 선택한 내 여행 필터 유형
     * @param userPrincipal 커스텀 어노테이션으로 주입된 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 필터별 여행 카드 목록 응답
     */
    @GetMapping("/filter")
    @ApiErrorExceptions({ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "내 여행 필터별 조회",
            description = "내 여행 페이지의 전체, 예정된 여행, 지난 여행 탭에 맞는 여행 카드 목록을 조회합니다."
    )
    public ApiResponse<MyTripListResponseDto> getMyTripsByFilter(
            @Parameter(description = "내 여행 필터 유형", example = "UPCOMING")
            @RequestParam(name = "filterType", defaultValue = "ALL") MyTripFilterType filterType,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        MyTripFilterRequestDto requestDto = MyTripFilterRequestDto.of(userPrincipal.getUsersId(), filterType);
        return ApiResponse.ok(tripService.getMyTripsByFilter(requestDto));
    }

    /**
     * 홈 화면 상단 카드에 노출할 가까운 여행 일정 정보를 조회합니다.
     *
     * @param requestDto 조회 대상 사용자 PK를 담은 요청 DTO
     * @return 공통 응답 형식으로 감싼 가까운 여행 일정 응답
     */
    @PostMapping("/nearby-schedule")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "가까운 여행 일정 조회",
            description = "홈 화면 상단 카드에 노출할 가까운 여행 일정 정보를 조회합니다."
    )
    public ApiResponse<NearbyTripScheduleResponseDto> getNearbyTripSchedule(
            @Valid @RequestBody NearbyTripScheduleRequestDto requestDto
    ) {
        return ApiResponse.ok(tripService.getNearbyTripSchedule(requestDto));
    }
}
