package mars.tripplanappbackend.trip.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.config.auth.CurrentUser;
import mars.tripplanappbackend.global.config.auth.UserPrincipal;
import mars.tripplanappbackend.global.config.swagger.ApiErrorExceptions;
import mars.tripplanappbackend.global.dto.ApiResponse;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.trip.dto.request.CreateTripRequestDto;
import mars.tripplanappbackend.trip.dto.request.DeleteTripRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripFilterRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripListRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripScheduleByDateRequestDto;
import mars.tripplanappbackend.trip.dto.request.NearbyTripScheduleRequestDto;
import mars.tripplanappbackend.trip.dto.request.ShareTripRequestDto;
import mars.tripplanappbackend.trip.dto.request.UpdateTripRequestDto;
import mars.tripplanappbackend.trip.dto.response.CreateTripResponseDto;
import mars.tripplanappbackend.trip.dto.response.DeleteTripResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripListResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripScheduleByDateResponseDto;
import mars.tripplanappbackend.trip.dto.response.NearbyTripScheduleResponseDto;
import mars.tripplanappbackend.trip.dto.response.ShareTripResponseDto;
import mars.tripplanappbackend.trip.dto.response.UpdateTripResponseDto;
import mars.tripplanappbackend.trip.enums.MyTripFilterType;
import mars.tripplanappbackend.trip.service.TripService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 내 여행 페이지와 홈 화면 상단 카드에서 사용하는 여행 관련 API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Tag(name = "Trip", description = "여행 관련 API")
public class TripController {

    private final TripService tripService;

    /**
     * 내 여행 추가 화면에서 여행 이미지, 제목, 여행 기간을 입력받아 새 여행을 생성합니다.
     *
     * @param requestDto 여행 생성에 필요한 본문 요청 DTO
     * @param userPrincipal 커스텀 어노테이션으로 주입된 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 여행 추가 결과 응답
     */
    @PostMapping("/create")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "여행 추가",
            description = "내 여행 추가 화면에서 여행 이미지, 여행 제목, 여행 시작일, 여행 종료일을 입력받아 새 여행을 생성합니다."
    )
    public ApiResponse<CreateTripResponseDto> createTrip(
            @Valid @RequestBody CreateTripRequestDto requestDto,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        CreateTripRequestDto serviceRequestDto = CreateTripRequestDto.of(userPrincipal.getUsersId(), requestDto);
        return ApiResponse.ok(tripService.createTrip(serviceRequestDto));
    }

    /**
     * 내 여행 상세 화면에서 여행 대표 이미지, 제목, 여행 기간을 수정합니다.
     * 시안상 "편집 -> 여행 추가 페이지 -> 저장하기" 흐름을 그대로 반영해
     * 여행 추가 화면과 동일한 필드 구조로 수정 요청을 받습니다.
     *
     * @param tripId 수정할 여행 PK
     * @param requestDto 수정할 여행 정보를 담은 본문 요청 DTO
     * @param userPrincipal 커스텀 애노테이션으로 주입된 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 여행 수정 결과 응답
     */
    @PatchMapping("/{tripId}")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "내 일정 수정",
            description = "내 여행 상세 화면에서 여행 이미지, 여행 제목, 여행 시작일, 여행 종료일을 수정합니다."
    )
    public ApiResponse<UpdateTripResponseDto> updateTrip(
            @Parameter(description = "수정할 여행 PK", example = "1")
            @PathVariable("tripId") Long tripId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "여행 상세 화면에서 수정할 여행 기본 정보입니다.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "여행 수정 예시",
                                    value = """
                                            {
                                              "title": "오사카 여행",
                                              "imageUrl": "https://cdn.lets-trip.com/trips/osaka.jpg",
                                              "startDate": "2026-04-06",
                                              "endDate": "2026-04-14"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody UpdateTripRequestDto requestDto,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
UpdateTripRequestDto serviceRequestDto =
                UpdateTripRequestDto.of(tripId, userPrincipal.getUsersId(), requestDto);
        return ApiResponse.ok(tripService.updateTrip(serviceRequestDto));
    }

    /**
     * 내 여행 상세 화면 상단 더보기 메뉴에서 선택한 여행을 삭제합니다.
     * 삭제는 별도 확인 화면이 아니라 상세 화면의 메뉴 액션으로 연결되는 흐름이므로,
     * 경로 변수의 여행 PK와 현재 로그인 사용자 정보를 조합해 서비스 계층으로 전달합니다.
     *
     * @param tripId 삭제할 여행 PK
     * @param userPrincipal 커스텀 애노테이션으로 주입된 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 여행 삭제 결과 응답
     */
    @DeleteMapping("/{tripId}")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "내 여행 삭제",
            description = "내 여행 상세 화면 상단 더보기 메뉴에서 현재 선택한 여행을 삭제합니다."
    )
    public ApiResponse<DeleteTripResponseDto> deleteTrip(
            @Parameter(description = "삭제할 여행 PK", example = "1")
            @PathVariable("tripId") Long tripId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        DeleteTripRequestDto requestDto = DeleteTripRequestDto.of(tripId, userPrincipal.getUsersId());
        return ApiResponse.ok(tripService.deleteTrip(requestDto));
    }

    /**
     * 내 여행 상세 화면 상단 더보기 메뉴에서 공유 시트에 필요한 여행 공유 정보를 조회합니다.
     * 여행 제목, 공유 설명, 공유 링크, 대표 이미지를 함께 내려 공유 UI에서 바로 사용할 수 있도록 구성합니다.
     *
     * @param tripId 공유할 여행 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입된 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 여행 공유 응답
     */
    @GetMapping("/{tripId}/share")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "내 여행 공유",
            description = "내 여행 상세 화면 상단 더보기 메뉴에서 사용한 공유 메타데이터를 조회합니다."
    )
    public ApiResponse<ShareTripResponseDto> shareTrip(
            @Parameter(description = "공유할 여행 PK", example = "1")
            @PathVariable("tripId") Long tripId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        ShareTripRequestDto requestDto = ShareTripRequestDto.of(tripId, userPrincipal.getUsersId());
        return ApiResponse.ok(tripService.shareTrip(requestDto));
    }

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
     * 내 여행 페이지 상단 필터 탭에서 선택한 유형에 맞는 여행 카드 목록을 조회합니다.
     *
     * @param filterType 화면에서 선택한 여행 필터 유형
     * @param userPrincipal 커스텀 어노테이션으로 주입된 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 필터별 여행 카드 목록 응답
     */
    @GetMapping("/filter")
    @ApiErrorExceptions({ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "내 여행 필터별 조회",
            description = "내 여행 페이지에서 전체, 예정된 여행, 지난 여행 탭에 맞는 여행 카드 목록을 조회합니다."
    )
    public ApiResponse<MyTripListResponseDto> getMyTripsByFilter(
            @Parameter(description = "여행 필터 유형", example = "UPCOMING")
            @RequestParam(name = "filterType", defaultValue = "ALL") MyTripFilterType filterType,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        MyTripFilterRequestDto requestDto = MyTripFilterRequestDto.of(userPrincipal.getUsersId(), filterType);
        return ApiResponse.ok(tripService.getMyTripsByFilter(requestDto));
    }

    /**
     * 내 여행 페이지에서 선택한 날짜 기준으로 일정 드롭다운 정보와 일정 목록을 조회합니다.
     *
     * @param tripId 조회할 여행 PK
     * @param targetDate 조회할 일정 날짜
     * @param userPrincipal 커스텀 어노테이션으로 주입된 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 날짜별 일정 목록 응답
     */
    @GetMapping("/{tripId}/schedules/by-date")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "내 일정 날짜별 조회",
            description = "내 여행 페이지에서 선택한 날짜 기준으로 일정 드롭다운 정보와 해당 날짜 일정 목록을 조회합니다."
    )
    public ApiResponse<MyTripScheduleByDateResponseDto> getMyTripSchedulesByDate(
            @Parameter(description = "조회할 여행 PK", example = "7")
            @PathVariable("tripId") Long tripId,
            @Parameter(description = "조회할 일정 날짜", example = "2026-02-15")
            @RequestParam(name = "targetDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        MyTripScheduleByDateRequestDto requestDto =
                MyTripScheduleByDateRequestDto.of(tripId, userPrincipal.getUsersId(), targetDate);
        return ApiResponse.ok(tripService.getMyTripSchedulesByDate(requestDto));
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
