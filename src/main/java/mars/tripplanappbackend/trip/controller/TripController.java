package mars.tripplanappbackend.trip.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.config.swagger.ApiErrorExceptions;
import mars.tripplanappbackend.global.dto.ApiResponse;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.trip.dto.request.NearbyTripScheduleRequestDto;
import mars.tripplanappbackend.trip.dto.response.NearbyTripScheduleResponseDto;
import mars.tripplanappbackend.trip.service.TripService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메인 페이지와 여행 화면에서 사용하는 여행 조회 API를 제공한다.
 */
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Tag(name = "Trip", description = "여행 도메인")
public class TripController {

    private final TripService tripService;

    /**
     * 홈 화면 상단 카드에 노출할 가까운 여행일정 정보를 조회한다.
     *
     * @param requestDto 조회 대상 사용자 PK를 담은 요청 DTO
     * @return 가까운 여행일정 정보를 담은 공통 응답
     */
    @PostMapping("/nearby-schedule")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "가까운 여행일정 조회",
            description = "홈 화면 상단 카드에 노출할 가까운 여행일정 정보를 조회합니다."
    )
    public ApiResponse<NearbyTripScheduleResponseDto> getNearbyTripSchedule(
            @Valid @RequestBody NearbyTripScheduleRequestDto requestDto
    ) {
        return ApiResponse.ok(tripService.getNearbyTripSchedule(requestDto));
    }
}
