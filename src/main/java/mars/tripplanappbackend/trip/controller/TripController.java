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
import mars.tripplanappbackend.trip.dto.request.AddVisitedPlaceRequestDto;
import mars.tripplanappbackend.trip.dto.request.AddWishlistPlaceRequestDto;
import mars.tripplanappbackend.trip.dto.request.CreateTripRequestDto;
import mars.tripplanappbackend.trip.dto.request.DeleteTripRequestDto;
import mars.tripplanappbackend.trip.dto.request.DeleteTripScheduleRequestDto;
import mars.tripplanappbackend.trip.dto.request.DeleteWishlistPlaceRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripFilterRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripListRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripScheduleByDateRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripDetailRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripMapSearchRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripScheduleListRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripScheduleLocationRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripScheduleRouteRequestDto;
import mars.tripplanappbackend.trip.dto.request.NearbyTripScheduleRequestDto;
import mars.tripplanappbackend.trip.dto.request.ShareTripRequestDto;
import mars.tripplanappbackend.trip.dto.request.TripPlaceSelectionRequestDto;
import mars.tripplanappbackend.trip.dto.request.UpdateTripRequestDto;
import mars.tripplanappbackend.trip.dto.response.AddVisitedPlaceResponseDto;
import mars.tripplanappbackend.trip.dto.response.AddWishlistPlaceResponseDto;
import mars.tripplanappbackend.trip.dto.response.CreateTripResponseDto;
import mars.tripplanappbackend.trip.dto.response.DeleteTripResponseDto;
import mars.tripplanappbackend.trip.dto.response.DeleteTripScheduleResponseDto;
import mars.tripplanappbackend.trip.dto.response.DeleteWishlistPlaceResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripDetailResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripListResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripMapSearchResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripScheduleByDateResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripScheduleListResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripScheduleLocationResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripScheduleRouteResponseDto;
import mars.tripplanappbackend.trip.dto.response.NearbyTripScheduleResponseDto;
import mars.tripplanappbackend.trip.dto.response.ShareTripResponseDto;
import mars.tripplanappbackend.trip.dto.response.TripPlaceSelectionResponseDto;
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
import mars.tripplanappbackend.trip.dto.request.UpdateTripDateRequestDto;
import mars.tripplanappbackend.trip.dto.response.UpdateTripDateResponseDto;
import java.time.LocalDate;

/**
 * 여행 페이지와 내 여행 상세 화면에서 사용하는 여행 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Tag(name = "Trip", description = "여행 관련 API")
public class TripController {

    private final TripService tripService;

    /**
     * 여행 추가 화면에서 여행 이미지, 제목, 여행 시작일과 종료일을 입력받아 새 여행을 생성합니다.
     *
     * @param requestDto 여행 생성 요청 본문 DTO
     * @param userPrincipal 커스텀 어노테이션으로 주입한 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 여행 생성 결과
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
     *
     * @param tripId 수정할 여행 PK
     * @param requestDto 수정할 여행 기본 정보를 담은 본문 요청 DTO
     * @param userPrincipal 커스텀 어노테이션으로 주입한 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 여행 수정 결과
     */
    @PatchMapping("/{tripId}")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "내 일정 수정",
            description = "내 여행 상세 화면에서 여행 대표 이미지, 여행 제목, 여행 시작일, 여행 종료일을 수정합니다."
    )
    public ApiResponse<UpdateTripResponseDto> updateTrip(
            @Parameter(description = "수정할 여행 PK", example = "1")
            @PathVariable("tripId") Long tripId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "내 여행 상세 화면에서 수정할 여행 기본 정보입니다.",
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
     * 내 여행 상세 화면 상단 더보기 메뉴에서 현재 선택한 여행을 삭제합니다.
     *
     * @param tripId 삭제할 여행 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입한 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 여행 삭제 결과
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
     * 내 여행 상세 화면 상단 더보기 메뉴에서 사용할 여행 공유 정보를 조회합니다.
     *
     * @param tripId 공유할 여행 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입한 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 여행 공유 응답
     */
    @GetMapping("/{tripId}/share")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "내 여행 공유",
            description = "내 여행 상세 화면 상단 더보기 메뉴에서 사용할 공유 메타데이터를 조회합니다."
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
     * 홈/내 여행 화면에서 선택한 여행의 상세 화면 전체를 구성할 데이터를 조회합니다.
     * 여행 기본 정보, 현재 진행 중 일정 요약, 지도 보기/편집/일정 추가 가능 여부, 일차별 일정 목록을 함께 반환합니다.
     *
     * @param tripId 조회할 여행 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입한 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 내 여행 상세 조회 결과
     */
    @GetMapping("/{tripId}")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "내 일정 상세 조회",
            description = "홈/내 여행 화면에서 선택한 여행의 상세 화면 전체를 구성하기 위한 여행 기본 정보, 현재 진행 중 일정 요약, 액션 가능 상태, 일차별 일정 목록을 조회합니다."
    )
    public ApiResponse<MyTripDetailResponseDto> findOne(
            @Parameter(description = "조회할 여행 PK", example = "5")
            @PathVariable("tripId") Long tripId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        MyTripDetailRequestDto requestDto = MyTripDetailRequestDto.of(tripId, userPrincipal.getUsersId());
        return ApiResponse.ok(tripService.findOne(requestDto));
    }

    /**
     * 내 여행 상세 화면 진입 시 일차별 일정 리스트를 한 번에 조회합니다.
     * 여행 기간 전체(1일차 ~ n일차)를 기준으로 섹션을 구성하고, 일정이 없는 날짜도 빈 배열로 유지해
     * 프론트엔드가 "일정 추가하기" UI를 자연스럽게 렌더링할 수 있도록 합니다.
     * 또한 일정별 진행 상태(isOngoing), 방문 기록 여부, 방문지 저장 버튼 노출 가능 여부를 함께 반환합니다.
     *
     * @param tripId 조회할 여행 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입한 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 내 여행 상세 일정 리스트 조회 결과
     */
    @GetMapping("/{tripId}/schedules")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "일정 리스트 조회",
            description = "여행 상세 화면에서 사용하는 일차별 일정 묶음 데이터를 조회합니다. 여행 기간 전체 날짜를 유지하고, 각 일정의 진행 상태와 방문 기록 상태를 함께 반환합니다."
    )
    public ApiResponse<MyTripScheduleListResponseDto> getMyTripSchedules(
            @Parameter(description = "조회할 여행 PK", example = "5")
            @PathVariable("tripId") Long tripId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        MyTripScheduleListRequestDto requestDto =
                MyTripScheduleListRequestDto.of(tripId, userPrincipal.getUsersId());
        return ApiResponse.ok(tripService.getMyTripSchedules(requestDto));
    }

    /**
     * 내 여행 상세의 날짜 카드에서 장소 추가하기 버튼을 눌렀을 때 표시할
     * 저장한 장소/위시리스트 탭 데이터를 함께 조회합니다.
     * 저장한 장소 탭에서는 현재 여행 위시리스트에 이미 담긴 장소인지 여부와
     * 버튼 라벨(담기/취소)을 함께 반환하고, 각 탭이 비어 있을 때는 빈 상태 메시지도 함께 반환합니다.
     *
     * @param tripId 조회할 여행 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입한 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 저장한 장소/위시리스트 조회 응답
     */
    @GetMapping("/{tripId}/place-selection")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "저장한 장소/위시리스트 조회",
            description = "여행 상세 바텀시트의 저장한 장소/위시리스트 탭 데이터를 함께 조회합니다. 저장한 장소의 위시 담김 상태와 탭별 빈 상태 메시지를 함께 반환합니다."
    )
    public ApiResponse<TripPlaceSelectionResponseDto> getTripPlaceSelection(
            @Parameter(description = "조회할 여행 PK", example = "5")
            @PathVariable("tripId") Long tripId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        TripPlaceSelectionRequestDto requestDto =
                TripPlaceSelectionRequestDto.of(tripId, userPrincipal.getUsersId());
        return ApiResponse.ok(tripService.getTripPlaceSelection(requestDto));
    }

    /**
     * 내 여행지 상세의 지도 검색창 키워드로 장소 후보를 조회합니다.
     * 지도 핀 표시를 위한 좌표와 리스트 카드 렌더링 정보를 함께 반환하며,
     * 현재 여행 위시리스트 담김 여부와 버튼 상태(담기/취소)도 같이 전달합니다.
     *
     * @param tripId 조회 대상 여행 PK
     * @param keyword 지도 검색어(미입력/공백이면 빈 결과 반환)
     * @param userPrincipal 커스텀 애노테이션으로 주입된 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 내 여행지 상세 지도 검색 결과
     */
    @GetMapping("/{tripId}/map-search")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "지도 검색 결과",
            description = "내 여행지 상세의 장소 선택 단계에서 검색어 기준 장소 후보를 조회합니다. " +
                    "지도 핀 좌표, 카드 정보, 위시리스트 담김 상태를 함께 반환합니다."
    )
    public ApiResponse<MyTripMapSearchResponseDto> getMyTripMapSearchResults(
            @Parameter(description = "조회 대상 여행 PK", example = "5")
            @PathVariable("tripId") Long tripId,
            @Parameter(description = "지도 검색어", example = "오사카성")
            @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        MyTripMapSearchRequestDto requestDto =
                MyTripMapSearchRequestDto.of(tripId, userPrincipal.getUsersId(), keyword);
        return ApiResponse.ok(tripService.getMyTripMapSearchResults(requestDto));
    }

    /**
     * 내 여행 페이지 전체 탭에서 노출되는 여행 카드 목록을 조회합니다.
     *
     * @param userPrincipal 커스텀 어노테이션으로 주입한 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 내 여행 전체 목록
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
     * 내 여행 페이지 상단 필터에서 선택한 유형에 맞는 여행 카드 목록을 조회합니다.
     *
     * @param filterType 화면에서 선택한 여행 필터 유형
     * @param userPrincipal 커스텀 어노테이션으로 주입한 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 필터별 여행 목록
     */
    @GetMapping("/filter")
    @ApiErrorExceptions({ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "내 여행 필터별 조회",
            description = "내 여행 페이지에서 전체, 예정된 여행, 지난 여행 필터에 맞는 여행 카드 목록을 조회합니다."
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
     * 내 여행 페이지에서 선택한 날짜 기준으로 일정 드롭다운 정보와 해당 날짜 일정 목록을 조회합니다.
     *
     * @param tripId 조회할 여행 PK
     * @param targetDate 조회할 일정 날짜
     * @param userPrincipal 커스텀 어노테이션으로 주입한 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 날짜별 일정 목록
     */
    @GetMapping("/{tripId}/schedules/by-date")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "내 일정 날짜별 조회",
            description = "내 여행 페이지에서 선택한 날짜 기준으로 일정 드롭다운 정보와 해당 날짜 일정 목록을 조회합니다."
    )
    public ApiResponse<MyTripScheduleByDateResponseDto> getMyTripSchedulesByDate(
            @Parameter(description = "조회할 여행 PK", example = "5")
            @PathVariable("tripId") Long tripId,
            @Parameter(description = "조회할 일정 날짜", example = "2026-04-20")
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
     * 내 여행 상세 화면에서 지도 보기 버튼을 눌렀을 때 지도 페이지에 필요한 일정 위치 목록을 조회합니다.
     * 일정별 좌표, 현재 진행 중 일정 여부, 방문 인증 버튼 노출 여부, 핀 순서를 함께 내려주어
     * 프론트엔드가 현재 일정 강조, 이동 동선 연결, GPS 기반 방문 인증 UI를 한 번에 구성할 수 있도록 합니다.
     *
     * @param tripId 조회할 여행 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입한 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 지도 페이지용 일정 위치 조회 응답
     */
    @GetMapping("/{tripId}/schedules/locations")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "일정 위치 조회",
            description = "내 여행 상세 화면에서 지도 보기 버튼을 눌렀을 때 일정별 핀, 이동 동선, 현재 일정 표시, GPS 방문 인증 UI에 필요한 일정 위치 목록을 조회합니다."
    )
    public ApiResponse<MyTripScheduleLocationResponseDto> getMyTripScheduleLocations(
            @Parameter(description = "조회할 여행 PK", example = "5")
            @PathVariable("tripId") Long tripId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        MyTripScheduleLocationRequestDto requestDto =
                MyTripScheduleLocationRequestDto.of(tripId, userPrincipal.getUsersId());
        return ApiResponse.ok(tripService.getMyTripScheduleLocations(requestDto));
    }

    /**
     * 내 여행 상세 화면의 특정 일정에서 "길찾기"를 눌렀을 때 사용할 목적지 정보를 조회합니다.
     * 일정에 연결된 장소 데이터를 검증한 뒤, 구글 길찾기 앱으로 연결 가능한 URL을 함께 반환합니다.
     *
     * @param tripId 조회할 여행 PK
     * @param tripScheduleId 길찾기 대상 일정 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입한 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 길찾기 목적지 정보 응답
     */
    @GetMapping("/{tripId}/schedules/{tripScheduleId}/route")
    @ApiErrorExceptions({
            ErrorCode.INVALID_INPUT,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.INTERNAL_ERROR
    })
    @Operation(
            summary = "길찾기",
            description = "여행 상세 일정의 장소 정보를 기반으로 목적지 검증 후 구글 길찾기 연결 정보를 반환합니다."
    )
    public ApiResponse<MyTripScheduleRouteResponseDto> getMyTripScheduleRoute(
            @Parameter(description = "조회할 여행 PK", example = "5")
            @PathVariable("tripId") Long tripId,
            @Parameter(description = "길찾기 대상 일정 PK", example = "7")
            @PathVariable("tripScheduleId") Long tripScheduleId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        MyTripScheduleRouteRequestDto requestDto =
                MyTripScheduleRouteRequestDto.of(tripId, tripScheduleId, userPrincipal.getUsersId());
        return ApiResponse.ok(tripService.getMyTripScheduleRoute(requestDto));
    }

    /**
     * 내 여행지 상세 화면에서 현재 장소를 방문 인증 기록으로 저장합니다.
     * 단순 저장 탭용 찜이 아니라 여행 중 실제로 방문한 장소를 기록하는 용도이며,
     * 여행 PK, 일정 PK, 장소 PK, 로그인 사용자 정보가 모두 맞을 때만 저장합니다.
     *
     * @param tripId 방문 기록을 저장할 여행 PK
     * @param requestDto 방문한 장소 PK와 연결할 일정 PK를 담은 요청 본문 DTO
     * @param userPrincipal 커스텀 어노테이션으로 주입된 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 방문 기록 저장 결과
     */
    @PostMapping("/{tripId}/visited-places")
    @ApiErrorExceptions({
            ErrorCode.INVALID_INPUT,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.VISITED_PLACE_ALREADY_EXISTS,
            ErrorCode.INTERNAL_ERROR
    })
    @Operation(
            summary = "방문 기록 저장",
            description = "내 여행지 상세 화면에서 현재 장소를 방문 인증 기록으로 저장합니다."
    )
    public ApiResponse<AddVisitedPlaceResponseDto> addVisitedPlace(
            @Parameter(description = "방문 기록을 저장할 여행 PK", example = "5")
            @PathVariable("tripId") Long tripId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "방문한 장소 PK와 연결할 일정 PK를 담은 요청 본문입니다.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "방문 기록 저장 예시",
                                    value = """
                                            {
                                              "placeId": 8,
                                              "tripScheduleId": 7
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody AddVisitedPlaceRequestDto requestDto,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        AddVisitedPlaceRequestDto serviceRequestDto =
                AddVisitedPlaceRequestDto.of(tripId, userPrincipal.getUsersId(), requestDto);
        return ApiResponse.ok(tripService.addVisitedPlace(serviceRequestDto));
    }

    /**
     * 내 여행 상세 화면에서 날짜별 일정 카드의 추가하기 버튼을 통해 선택한 장소를 해당 여행의 위시리스트에 추가합니다.
     *
     * @param tripId 위시리스트 장소를 추가할 여행 PK
     * @param requestDto 선택한 장소 PK와 날짜 카드 기준 일정 날짜를 담은 본문 DTO
     * @param userPrincipal 커스텀 어노테이션으로 주입한 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 위시리스트 장소 추가 결과
     */
    @PostMapping("/{tripId}/wishlist-places")
    @ApiErrorExceptions({
            ErrorCode.INVALID_INPUT,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.WISHLIST_PLACE_ALREADY_EXISTS,
            ErrorCode.INTERNAL_ERROR
    })
    @Operation(
            summary = "위시리스트 장소 추가",
            description = "내 여행 상세 화면에서 날짜별 일정 카드의 추가하기 버튼을 통해 선택한 장소를 해당 여행의 위시리스트에 추가합니다."
    )
    public ApiResponse<AddWishlistPlaceResponseDto> addWishlistPlace(
            @Parameter(description = "위시리스트 장소를 추가할 여행 PK", example = "5")
            @PathVariable("tripId") Long tripId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "장소 선택/지도 화면에서 선택한 장소와 날짜 카드 기준 정보를 담은 요청입니다.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "위시리스트 장소 추가 예시",
                                    value = """
                                            {
                                              "placeId": 7,
                                              "scheduleDate": "2026-04-20"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody AddWishlistPlaceRequestDto requestDto,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        AddWishlistPlaceRequestDto serviceRequestDto =
                AddWishlistPlaceRequestDto.of(tripId, userPrincipal.getUsersId(), requestDto);
        return ApiResponse.ok(tripService.addWishlistPlace(serviceRequestDto));
    }

    /**
     * 내 여행 상세 화면의 날짜 카드에 추가된 위시리스트 장소를 개별 메뉴를 통해 삭제합니다.
     *
     * @param tripId 위시리스트 장소가 속한 여행 PK
     * @param wishlistPlaceId 삭제할 위시리스트 장소 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입한 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 위시리스트 장소 삭제 결과
     */
    @DeleteMapping("/{tripId}/wishlist-places/{wishlistPlaceId}")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "위시리스트 장소 삭제",
            description = "내 여행 상세 화면의 날짜 카드에 추가된 위시리스트 장소를 개별 메뉴를 통해 삭제합니다."
    )
    public ApiResponse<DeleteWishlistPlaceResponseDto> deleteWishlistPlace(
            @Parameter(description = "위시리스트 장소가 속한 여행 PK", example = "5")
            @PathVariable("tripId") Long tripId,
            @Parameter(description = "삭제할 위시리스트 장소 PK", example = "11")
            @PathVariable("wishlistPlaceId") Long wishlistPlaceId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        DeleteWishlistPlaceRequestDto requestDto =
                DeleteWishlistPlaceRequestDto.of(tripId, wishlistPlaceId, userPrincipal.getUsersId());
        return ApiResponse.ok(tripService.deleteWishlistPlace(requestDto));
    }

    /**
     * 내 여행 상세 화면의 일정 리스트에서 개별 일정 메뉴를 통해 선택한 일정을 삭제합니다.
     *
     * @param tripId 일정이 속한 여행 PK
     * @param tripScheduleId 삭제할 개별 일정 PK
     * @param userPrincipal 커스텀 어노테이션으로 주입한 현재 로그인 사용자 정보
     * @return 공통 응답 형식으로 감싼 일정 삭제 결과
     */
    @DeleteMapping("/{tripId}/schedules/{tripScheduleId}")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "일정 삭제",
            description = "내 여행 상세 화면의 일정 리스트에서 개별 일정 메뉴를 통해 선택한 일정을 삭제합니다."
    )
    public ApiResponse<DeleteTripScheduleResponseDto> deleteTripSchedule(
            @Parameter(description = "일정이 속한 여행 PK", example = "7")
            @PathVariable("tripId") Long tripId,
            @Parameter(description = "삭제할 개별 일정 PK", example = "21")
            @PathVariable("tripScheduleId") Long tripScheduleId,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal userPrincipal
    ) {
        DeleteTripScheduleRequestDto requestDto =
                DeleteTripScheduleRequestDto.of(tripId, tripScheduleId, userPrincipal.getUsersId());
        return ApiResponse.ok(tripService.deleteTripSchedule(requestDto));
    }

    /**
     * 메인 화면 상단 카드에서 사용하는 가까운 여행 일정 정보를 조회합니다.
     *
     * @param requestDto 조회 대상 사용자 PK를 담은 요청 DTO
     * @return 공통 응답 형식으로 감싼 가까운 여행 일정 응답
     */
    @PostMapping("/nearby-schedule")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(
            summary = "가까운 여행일정 조회",
            description = "메인 화면 상단 카드에 노출할 가까운 여행 일정 정보를 조회합니다."
    )
    public ApiResponse<NearbyTripScheduleResponseDto> getNearbyTripSchedule(
            @Valid @RequestBody NearbyTripScheduleRequestDto requestDto
    ) {
        return ApiResponse.ok(tripService.getNearbyTripSchedule(requestDto));
    }

    /**
     * 여행 날짜 수정 API
     *
     * PATCH /api/v1/trips/{tripId}/date
     */
    @PatchMapping("/{tripId}/date")
    public UpdateTripDateResponseDto updateTripDate(
            @PathVariable Long tripId,
            @RequestBody UpdateTripDateRequestDto requestDto
    ) {
        return tripService.updateTripDate(tripId, requestDto);
    }
}
