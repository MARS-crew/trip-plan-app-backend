package mars.tripplanappbackend.trip.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import mars.tripplanappbackend.mypage.repository.SavedPlaceRepository;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.enums.PlaceType;
import mars.tripplanappbackend.place.repository.PlaceRepository;
import mars.tripplanappbackend.search.service.GooglePlaceSearchService;
import mars.tripplanappbackend.trip.dto.request.AddTripScheduleRequestDto;
import mars.tripplanappbackend.trip.dto.request.AddVisitedPlaceRequestDto;
import mars.tripplanappbackend.trip.dto.request.AddWishlistPlaceRequestDto;
import mars.tripplanappbackend.trip.domain.Trip;
import mars.tripplanappbackend.trip.domain.TripSchedule;
import mars.tripplanappbackend.trip.domain.VisitedPlace;
import mars.tripplanappbackend.trip.domain.WishlistPlace;
import mars.tripplanappbackend.trip.dto.request.CreateTripRequestDto;
import mars.tripplanappbackend.trip.dto.request.DeleteTripRequestDto;
import mars.tripplanappbackend.trip.dto.request.DeleteTripScheduleRequestDto;
import mars.tripplanappbackend.trip.dto.request.DeleteWishlistPlaceRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripDetailRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripFilterRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripListRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripScheduleByDateRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripScheduleListRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripScheduleLocationRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripScheduleRouteRequestDto;
import mars.tripplanappbackend.trip.dto.request.NearbyTripScheduleRequestDto;
import mars.tripplanappbackend.trip.dto.request.ShareTripRequestDto;
import mars.tripplanappbackend.trip.dto.request.TripPlaceSelectionRequestDto;
import mars.tripplanappbackend.trip.dto.request.UpdateTripScheduleRequestDto;
import mars.tripplanappbackend.trip.dto.request.UpdateTripRequestDto;
import mars.tripplanappbackend.trip.dto.response.AddTripScheduleResponseDto;
import mars.tripplanappbackend.trip.dto.response.AddVisitedPlaceResponseDto;
import mars.tripplanappbackend.trip.dto.response.AddWishlistPlaceResponseDto;
import mars.tripplanappbackend.trip.dto.response.CreateTripResponseDto;
import mars.tripplanappbackend.trip.dto.response.DeleteTripResponseDto;
import mars.tripplanappbackend.trip.dto.response.DeleteTripScheduleResponseDto;
import mars.tripplanappbackend.trip.dto.response.DeleteWishlistPlaceResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripCurrentScheduleResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripDailyScheduleResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripDetailResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripListResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripScheduleByDateResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripScheduleDateOptionResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripScheduleItemResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripScheduleListResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripScheduleLocationItemResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripScheduleLocationResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripScheduleRouteResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripSummaryResponseDto;
import mars.tripplanappbackend.trip.dto.response.NearbyTripScheduleItemResponseDto;
import mars.tripplanappbackend.trip.dto.response.NearbyTripScheduleResponseDto;
import mars.tripplanappbackend.trip.dto.response.ShareTripResponseDto;
import mars.tripplanappbackend.trip.dto.response.TripPlaceSelectionItemResponseDto;
import mars.tripplanappbackend.trip.dto.response.TripPlaceSelectionResponseDto;
import mars.tripplanappbackend.trip.dto.response.UpdateTripScheduleResponseDto;
import mars.tripplanappbackend.trip.dto.response.UpdateTripResponseDto;
import mars.tripplanappbackend.trip.enums.MyTripFilterType;
import mars.tripplanappbackend.trip.enums.TripStatus;
import mars.tripplanappbackend.trip.enums.WishlistSourceType;
import mars.tripplanappbackend.trip.repository.TripRepository;
import mars.tripplanappbackend.trip.repository.TripScheduleRepository;
import mars.tripplanappbackend.trip.repository.VisitedPlaceRepository;
import mars.tripplanappbackend.trip.repository.WishlistPlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 홈 화면과 내 여행 페이지에서 사용하는 여행 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {

    private static final long VISIT_VERIFICATION_RADIUS_METERS = 1_000L;
    private static final String TRIP_SHARE_URL_TEMPLATE = "https://lets-trip.com/trips/share/%s";
    private static final String TRIP_SHARE_TITLE_TEMPLATE = "Let's Trip에서 %s 일정을 확인해보세요.";
    private static final String TRIP_SHARE_DESCRIPTION_TEMPLATE = "%s부터 %s까지의 %s 일정을 공유합니다.";
    private static final String GOOGLE_DIRECTIONS_BASE_URL = "https://www.google.com/maps/dir/?api=1";
    private static final String GOOGLE_DIRECTIONS_COORDINATE_QUERY_TEMPLATE = "%s&destination=%s%%2C%s";
    private static final String GOOGLE_DIRECTIONS_ADDRESS_QUERY_TEMPLATE = "%s&destination=%s";
    private static final DateTimeFormatter SHARE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final ZoneId APP_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final int PLACE_NAME_MAX_LENGTH = 80;
    private static final int ADDRESS_MAX_LENGTH = 255;
    private static final int DESCRIPTION_MAX_LENGTH = 2000;
    private static final int IMAGE_URL_MAX_LENGTH = 500;
    private static final double SCHEDULE_LOCATION_NEARBY_SEARCH_RADIUS_METERS = 500.0d;
    private static final int SCHEDULE_LOCATION_NEARBY_RESULT_COUNT = 10;

    private final MyPageRepository myPageRepository;
    private final SavedPlaceRepository savedPlaceRepository;
    private final PlaceRepository placeRepository;
    private final GooglePlaceSearchService googlePlaceSearchService;
    private final TripRepository tripRepository;
    private final TripScheduleRepository tripScheduleRepository;
    private final WishlistPlaceRepository wishlistPlaceRepository;
    private final VisitedPlaceRepository visitedPlaceRepository;

    /**
     * 내 여행 페이지 전체 탭에 사용하는 여행 카드 목록을 조회합니다.
     *
     * @param requestDto 로그인 사용자 아이디를 담은 전체 리스트 조회 요청 DTO
     * @return 전체 여행 카드 목록 응답 DTO
     */
    public MyTripListResponseDto getMyTrips(MyTripListRequestDto requestDto) {
        return getMyTripListResponse(requestDto.getUsersId(), MyTripFilterType.ALL);
    }

    /**
     * 내 여행 상세 화면에서 선택한 여행의 기본 정보를 수정합니다.
     * 여행 추가 화면과 동일한 입력 구조를 사용하므로 제목, 대표 이미지, 여행 기간을 한 번에 갱신하고,
     * 기존 일정이 수정된 여행 기간을 벗어나는 경우에는 잘못된 요청으로 처리합니다.
     *
     * @param requestDto 수정 대상 여행 PK, 로그인 사용자 아이디, 수정할 여행 정보를 담은 요청 DTO
     * @return 수정된 여행 카드 정보를 담은 응답 DTO
     */
    @Transactional
    public UpdateTripResponseDto updateTrip(UpdateTripRequestDto requestDto) {
        validateUserExistsByUsersId(requestDto.getUsersId());
        validateUpdateTripDates(requestDto.getStartDate(), requestDto.getEndDate());

        Trip trip = tripRepository.findByTripIdAndUser_UsersIdAndIsDeletedFalse(
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        List<TripSchedule> schedules = tripScheduleRepository
                .findAllByTrip_TripIdAndIsDeletedFalseOrderByScheduleDateAscStartTimeAsc(trip.getTripId());

        validateScheduleDatesWithinTripRange(schedules, requestDto.getStartDate(), requestDto.getEndDate());

        TripStatus tripStatus = resolveTripStatus(requestDto.getStartDate(), requestDto.getEndDate(), currentDate());

        trip.updateTrip(
                requestDto.getTitle().trim(),
                requestDto.getStartDate(),
                requestDto.getEndDate(),
                normalizeImageUrl(requestDto.getImageUrl()),
                tripStatus
        );

        refreshTripScheduleDayNumbers(schedules, requestDto.getStartDate());

        return UpdateTripResponseDto.from(trip, tripStatus, schedules.size());
    }

    /**
     * 내 여행 상세 화면 상단 더보기 메뉴에서 선택한 여행을 삭제합니다.
     * 여행만 숨기면 연결된 일정과 장소 데이터가 남아 이후 조회 흐름과 충돌할 수 있으므로,
     * 같은 트랜잭션 안에서 여행과 연결된 일정, 찜한 장소, 방문 장소를 함께 soft delete 처리합니다.
     *
     * @param requestDto 삭제 대상 여행 PK와 현재 로그인 사용자 아이디를 담은 요청 DTO
     * @return 삭제 처리된 여행 정보를 담은 응답 DTO
     */
    @Transactional
    public DeleteTripResponseDto deleteTrip(DeleteTripRequestDto requestDto) {
        validateUserExistsByUsersId(requestDto.getUsersId());

        Trip trip = tripRepository.findByTripIdAndUser_UsersIdAndIsDeletedFalse(
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        List<TripSchedule> schedules =
                tripScheduleRepository.findAllByTrip_TripIdAndIsDeletedFalseOrderByScheduleDateAscStartTimeAsc(trip.getTripId());
        List<WishlistPlace> wishlistPlaces =
                wishlistPlaceRepository.findAllByTrip_TripIdAndIsDeletedFalse(trip.getTripId());
        List<VisitedPlace> visitedPlaces =
                visitedPlaceRepository.findAllByTrip_TripIdAndIsDeletedFalse(trip.getTripId());

        trip.markDeleted();
        schedules.forEach(TripSchedule::markDeleted);
        wishlistPlaces.forEach(WishlistPlace::markDeleted);
        visitedPlaces.forEach(VisitedPlace::markDeleted);

        return DeleteTripResponseDto.from(trip, schedules.size());
    }

    /**
     * 내 여행 상세 화면의 일정 리스트에서 개별 일정 메뉴를 통해 선택한 일정을 삭제합니다.
     * 여행 PK, 일정 PK, 로그인 사용자 아이디가 모두 일치하는 경우에만 삭제하도록 검증합니다.
     *
     * @param requestDto 여행 PK, 일정 PK, 로그인 사용자 아이디를 담은 요청 DTO
     * @return 삭제 처리된 일정 정보를 담은 응답 DTO
     */
    @Transactional
    public DeleteTripScheduleResponseDto deleteTripSchedule(DeleteTripScheduleRequestDto requestDto) {
        validateDeleteTripScheduleRequest(requestDto);
        validateUserExistsByUsersId(requestDto.getUsersId());

        TripSchedule tripSchedule = tripScheduleRepository
                .findByTripScheduleIdAndTrip_TripIdAndTrip_User_UsersIdAndIsDeletedFalse(
                        requestDto.getTripScheduleId(),
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        tripSchedule.markDeleted();

        return DeleteTripScheduleResponseDto.from(tripSchedule);
    }

    /**
     * 내 여행 상세 화면에 노출된 위시리스트 장소 카드를 개별 메뉴를 통해 삭제합니다.
     * 여행 PK, 위시리스트 PK, 로그인 사용자 아이디가 모두 일치하는 경우에만 삭제되도록 검증합니다.
     *
     * @param requestDto 여행 PK, 위시리스트 PK, 로그인 사용자 아이디를 담은 요청 DTO
     * @return 삭제 처리된 위시리스트 장소 정보를 담은 응답 DTO
     */
    @Transactional
    public DeleteWishlistPlaceResponseDto deleteWishlistPlace(DeleteWishlistPlaceRequestDto requestDto) {
        validateDeleteWishlistPlaceRequest(requestDto);
        validateUserExistsByUsersId(requestDto.getUsersId());

        WishlistPlace wishlistPlace = wishlistPlaceRepository
                .findByWishlistPlaceIdAndTrip_TripIdAndTrip_User_UsersIdAndIsDeletedFalse(
                        requestDto.getWishlistPlaceId(),
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        wishlistPlace.markDeleted();

        return DeleteWishlistPlaceResponseDto.from(wishlistPlace);
    }

    /**
     * 내 여행 상세 화면 상단 더보기 메뉴의 공유 항목에서 사용하는 공유 메타데이터를 생성합니다.
     * 본인 여행인지 검증한 뒤 공유 제목, 설명, 링크, 대표 이미지를 조합해 응답하고,
     * 공유 코드가 없는 경우 최초 한 번만 발급한 뒤 이후에는 같은 코드를 재사용합니다.
     *
     * @param requestDto 공유 대상 여행 PK와 현재 로그인 사용자 아이디를 담은 요청 DTO
     * @return 여행 공유 응답 DTO
     */
    @Transactional
    public ShareTripResponseDto shareTrip(ShareTripRequestDto requestDto) {
        validateShareTripRequest(requestDto);
        validateUserExistsByUsersId(requestDto.getUsersId());

        Trip trip = tripRepository.findByTripIdAndUser_UsersIdAndIsDeletedFalse(
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        String shareCode = issueUniqueTripShareCode(trip);

        return ShareTripResponseDto.from(
                trip,
                createTripShareTitle(trip),
                createTripShareDescription(trip),
                createTripShareUrl(shareCode)
        );
    }

    /**
     * 내 여행 페이지 상단 탭에서 선택한 필터 기준으로 여행 카드 목록을 조회합니다.
     * 예정된 여행 탭은 예정/진행 중 상태를 함께 반환하고,
     * 지난 여행 탭은 완료 상태 카드만 반환합니다.
     *
     * @param requestDto 로그인 사용자 아이디와 필터 유형을 담은 조회 요청 DTO
     * @return 필터 조건이 반영된 여행 카드 목록 응답 DTO
     */
    public MyTripListResponseDto getMyTripsByFilter(MyTripFilterRequestDto requestDto) {
        return getMyTripListResponse(requestDto.getUsersId(), requestDto.getFilterType());
    }

    /**
     * 여행 추가 화면에서 전달된 이미지, 제목, 여행 기간 정보로 새 여행을 생성합니다.
     *
     * @param requestDto 로그인 사용자 아이디와 여행 생성 본문 정보를 담은 요청 DTO
     * @return 생성된 여행 카드 정보를 담은 응답 DTO
     */
    @Transactional
    public CreateTripResponseDto createTrip(CreateTripRequestDto requestDto) {
        User user = findUserByUsersId(requestDto.getUsersId());
        validateCreateTripDates(requestDto.getStartDate(), requestDto.getEndDate());

        Trip trip = Trip.builder()
                .title(requestDto.getTitle().trim())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .imageUrl(normalizeImageUrl(requestDto.getImageUrl()))
                .tripStatus(resolveTripStatus(requestDto.getStartDate(), requestDto.getEndDate(), currentDate()))
                .user(user)
                .build();

        Trip savedTrip = tripRepository.save(trip);
        TripStatus tripStatus = resolveTripStatus(savedTrip, currentDate());

        return CreateTripResponseDto.from(savedTrip, tripStatus);
    }

    /**
     * 내 여행 페이지에서 선택한 날짜 기준으로 일정 목록을 조회합니다.
     * 조회 날짜를 전달하지 않으면 여행 시작일을 기본 조회 날짜로 사용합니다.
     *
     * @param requestDto 여행 PK, 사용자 아이디, 조회 날짜를 담은 요청 DTO
     * @return 날짜 드롭다운 정보와 해당 날짜 일정 목록 응답 DTO
     */
    public MyTripScheduleByDateResponseDto getMyTripSchedulesByDate(MyTripScheduleByDateRequestDto requestDto) {
        validateUserExistsByUsersId(requestDto.getUsersId());

        Trip trip = tripRepository.findByTripIdAndUser_UsersIdAndIsDeletedFalse(
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        LocalDate selectedDate = requestDto.getTargetDate() != null
                ? requestDto.getTargetDate()
                : trip.getStartDate();

        if (selectedDate.isBefore(trip.getStartDate()) || selectedDate.isAfter(trip.getEndDate())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        List<MyTripScheduleDateOptionResponseDto> dateOptions = buildDateOptions(trip);
        ZonedDateTime currentDateTime = currentDateTime();
        LocalDate today = currentDateTime.toLocalDate();
        LocalTime now = currentDateTime.toLocalTime();
        Set<Long> visitedPlaceIds = createVisitedPlaceIdSet(trip.getTripId());

        List<MyTripScheduleItemResponseDto> schedules = tripScheduleRepository
                .findAllByTrip_TripIdAndScheduleDateAndIsDeletedFalseOrderByStartTimeAsc(trip.getTripId(), selectedDate)
                .stream()
                .map(tripSchedule -> toTripScheduleItemResponse(tripSchedule, today, now, visitedPlaceIds))
                .toList();

        int selectedDayNo = calculateDayNo(trip.getStartDate(), selectedDate);

        return MyTripScheduleByDateResponseDto.of(
                trip.getTripId(),
                trip.getTitle(),
                selectedDate,
                selectedDayNo,
                dateOptions,
                schedules
        );
    }

    /**
     * 내 여행 상세 화면의 일정 리스트 영역을 구성하기 위한 일차별 일정 데이터를 조회합니다.
     * 여행 기간 전체 날짜를 1일차부터 순회하며 일정이 없는 날짜도 빈 배열로 유지해
     * 프론트엔드가 "일정 추가하기" 화면을 일관된 구조로 렌더링할 수 있도록 합니다.
     * 또한 각 일정 카드에 현재 진행 상태, 방문 기록 여부, 방문지 저장 버튼 노출 여부를 함께 계산합니다.
     *
     * @param requestDto 여행 PK와 로그인 사용자 아이디를 담은 일정 리스트 조회 요청 DTO
     * @return 내 여행 상세 화면용 일차별 일정 리스트 응답 DTO
     */
    public MyTripScheduleListResponseDto getMyTripSchedules(MyTripScheduleListRequestDto requestDto) {
        validateMyTripScheduleListRequest(requestDto);
        validateUserExistsByUsersId(requestDto.getUsersId());

        Trip trip = tripRepository.findByTripIdAndUser_UsersIdAndIsDeletedFalse(
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        List<TripSchedule> tripSchedules = tripScheduleRepository
                .findAllWithPlaceByTrip_TripIdAndIsDeletedFalseOrderByScheduleDateAscStartTimeAsc(trip.getTripId());

        ZonedDateTime currentDateTime = currentDateTime();
        LocalDate today = currentDateTime.toLocalDate();
        LocalTime now = currentDateTime.toLocalTime();
        Set<Long> visitedPlaceIds = createVisitedPlaceIdSet(trip.getTripId());
        long tripDayCount = ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;

        Map<LocalDate, List<TripSchedule>> schedulesByDate = tripSchedules.stream()
                .collect(Collectors.groupingBy(
                        TripSchedule::getScheduleDate,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<MyTripScheduleListResponseDto.DayScheduleResponseDto> dailySchedules =
                buildTripScheduleListDailySections(
                        trip,
                        schedulesByDate,
                        tripDayCount,
                        today,
                        now,
                        visitedPlaceIds
                );

        boolean hasOngoingSchedule = tripSchedules.stream()
                .anyMatch(tripSchedule -> isCurrentTripSchedule(tripSchedule, today, now));

        return MyTripScheduleListResponseDto.of(
                trip,
                tripDayCount,
                hasOngoingSchedule,
                dailySchedules
        );
    }

    /**
     * 홈/내 여행 화면에서 선택한 여행의 상세 화면 전체를 구성할 데이터를 조회합니다.
     * 여행 기본 정보와 일차별 일정 목록뿐 아니라 현재 진행 중 일정 요약과 화면 액션 가능 상태까지 함께 계산합니다.
     *
     * @param requestDto 조회 대상 여행 PK와 로그인 사용자 아이디를 담은 요청 DTO
     * @return 내 여행 상세 화면 전체를 구성하기 위한 응답 DTO
     */
    public MyTripDetailResponseDto findOne(MyTripDetailRequestDto requestDto) {
        validateTripDetailRequest(requestDto);
        validateUserExistsByUsersId(requestDto.getUsersId());

        Trip trip = tripRepository.findByTripIdAndUser_UsersIdAndIsDeletedFalse(
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        List<TripSchedule> tripSchedules = tripScheduleRepository
                .findAllWithPlaceByTrip_TripIdAndIsDeletedFalseOrderByScheduleDateAscStartTimeAsc(trip.getTripId());

        ZonedDateTime currentDateTime = currentDateTime();
        LocalDate today = currentDateTime.toLocalDate();
        LocalTime now = currentDateTime.toLocalTime();
        TripStatus tripStatus = resolveTripStatus(trip, today);
        Set<Long> visitedPlaceIds = createVisitedPlaceIdSet(trip.getTripId());
        int locationScheduleCount = countLocationSchedules(tripSchedules);
        MyTripCurrentScheduleResponseDto currentSchedule =
                findCurrentSchedule(tripSchedules, today, now, visitedPlaceIds);

        Map<LocalDate, List<MyTripScheduleItemResponseDto>> schedulesByDate = tripSchedules.stream()
                .collect(Collectors.groupingBy(
                        TripSchedule::getScheduleDate,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                tripSchedule -> toTripScheduleItemResponse(tripSchedule, today, now, visitedPlaceIds),
                                Collectors.toList()
                        )
                ));

        long tripDayCount = ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
        List<MyTripDailyScheduleResponseDto> dailySchedules =
                buildDailyScheduleSections(trip, schedulesByDate, tripDayCount);
        String tripDetailImageUrl = resolveTripDetailImageUrl(trip, tripSchedules);

        return MyTripDetailResponseDto.of(
                trip,
                tripDetailImageUrl,
                tripStatus,
                tripDayCount,
                tripSchedules.size(),
                locationScheduleCount,
                currentSchedule,
                canViewMap(locationScheduleCount),
                canEditTrip(trip),
                canAddSchedule(trip),
                dailySchedules
        );
    }

    /**
     * 내 여행 상세 화면에서 지도 보기 버튼을 눌렀을 때 사용할 일정 위치 목록을 조회합니다.
     * 일정별 장소 좌표, 핀 순서, 현재 진행 중 여부, 방문 인증 가능 여부를 함께 계산해
     * 지도 페이지에서 현재 일정 강조, 이동 동선 표시, 1km 반경 방문 인증 UI를 한 번에 구성할 수 있도록 합니다.
     *
     * @param requestDto 여행 PK와 로그인 사용자 아이디를 담은 일정 위치 조회 요청 DTO
     * @return 지도 페이지에서 사용할 일정 위치 조회 응답 DTO
     */
    @Transactional
    public MyTripScheduleLocationResponseDto getMyTripScheduleLocations(MyTripScheduleLocationRequestDto requestDto) {
        validateTripScheduleLocationRequest(requestDto);
        validateUserExistsByUsersId(requestDto.getUsersId());

        Trip trip = tripRepository.findByTripIdAndUser_UsersIdAndIsDeletedFalse(
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        List<TripSchedule> tripSchedules = tripScheduleRepository
                .findAllWithPlaceByTrip_TripIdAndIsDeletedFalseOrderByScheduleDateAscStartTimeAsc(trip.getTripId());

        ZonedDateTime currentDateTime = currentDateTime();
        LocalDate today = currentDateTime.toLocalDate();
        LocalTime now = currentDateTime.toLocalTime();
        TripStatus tripStatus = resolveTripStatus(trip, today);
        Set<Long> visitedPlaceIds = createVisitedPlaceIdSet(trip.getTripId());

        List<MyTripScheduleLocationItemResponseDto> schedules = buildTripScheduleLocationItems(
                tripSchedules,
                today,
                now,
                visitedPlaceIds
        );

        return MyTripScheduleLocationResponseDto.of(
                trip,
                tripStatus,
                VISIT_VERIFICATION_RADIUS_METERS,
                schedules
        );
    }

    /**
     * 내 여행 상세 화면의 특정 일정에 대해 외부 길찾기 앱 연결 정보를 조회합니다.
     * 일정 소유권(내 여행 여부)과 장소 데이터 유효성을 먼저 검증한 뒤,
     * 좌표가 있으면 좌표 기반 URL, 좌표가 없으면 주소 기반 URL로 구글 길찾기 연결 URL을 생성합니다.
     *
     * @param requestDto 여행 PK, 일정 PK, 로그인 사용자 아이디를 담은 길찾기 요청 DTO
     * @return 목적지 정보와 구글 길찾기 연결 URL을 담은 응답 DTO
     */
    public MyTripScheduleRouteResponseDto getMyTripScheduleRoute(MyTripScheduleRouteRequestDto requestDto) {
        validateMyTripScheduleRouteRequest(requestDto);
        validateUserExistsByUsersId(requestDto.getUsersId());

        TripSchedule tripSchedule = tripScheduleRepository
                .findByTripScheduleIdAndTrip_TripIdAndTrip_User_UsersIdAndIsDeletedFalse(
                        requestDto.getTripScheduleId(),
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        Place place = tripSchedule.getPlace();
        if (place == null || Boolean.TRUE.equals(place.getIsDeleted())) {
            throw new BusinessException(ErrorCode.PLACE_NOT_FOUND);
        }

        String destinationAddress = resolveScheduleAddress(tripSchedule);
        if (place.getLatitude() == null && place.getLongitude() == null && !hasText(destinationAddress)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        String googleDirectionsUrl = buildGoogleDirectionsUrl(
                place.getLatitude(),
                place.getLongitude(),
                destinationAddress
        );

        return MyTripScheduleRouteResponseDto.of(
                tripSchedule,
                destinationAddress,
                googleDirectionsUrl
        );
    }

    /**
     * 내 여행지 상세 화면에서 현재 장소를 방문 인증 기록으로 저장합니다.
     * 저장 탭용 찜과는 별개의 개념으로 처리하며, 현재 여행과 연결된 일정 카드 문맥 안에서
     * 실제 방문한 장소를 기록하는 용도로 사용합니다.
     *
     * @param requestDto 여행 PK, 일정 PK, 장소 PK, 로그인 사용자 아이디를 담은 요청 DTO
     * @return 저장된 방문 기록 정보를 담은 응답 DTO
     */
    @Transactional
    public AddVisitedPlaceResponseDto addVisitedPlace(AddVisitedPlaceRequestDto requestDto) {
        validateAddVisitedPlaceRequest(requestDto);
        validateUserExistsByUsersId(requestDto.getUsersId());

        User user = findUserByUsersId(requestDto.getUsersId());
        Trip trip = tripRepository.findByTripIdAndUser_UsersIdAndIsDeletedFalse(
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        TripSchedule tripSchedule = tripScheduleRepository
                .findByTripScheduleIdAndTrip_TripIdAndTrip_User_UsersIdAndIsDeletedFalse(
                        requestDto.getTripScheduleId(),
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        Place place = placeRepository.findByPlaceIdAndIsDeletedFalse(requestDto.getPlaceId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        if (tripSchedule.getPlace() != null && !tripSchedule.getPlace().getPlaceId().equals(place.getPlaceId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        if (visitedPlaceRepository.existsByTrip_TripIdAndPlace_PlaceIdAndIsDeletedFalse(
                trip.getTripId(),
                place.getPlaceId()
        )) {
            throw new BusinessException(ErrorCode.VISITED_PLACE_ALREADY_EXISTS);
        }

        VisitedPlace visitedPlace = visitedPlaceRepository.save(
                VisitedPlace.builder()
                        .user(user)
                        .trip(trip)
                        .tripSchedule(tripSchedule)
                        .place(place)
                        .build()
        );

        return AddVisitedPlaceResponseDto.from(visitedPlace);
    }

    /**
     * 내 여행지 상세 화면의 "일정 추가" 입력값을 기반으로 신규 일정을 생성합니다.
     * <p>
     * 처리 순서는 아래와 같습니다.
     * 1) 요청 필수값/형식 검증
     * 2) 사용자 및 여행 소유권 검증
     * 3) 여행 기간 내 날짜 검증
     * 4) 선택 장소 검증(선택값이 있을 때만)
     * 5) dayNo 계산 후 일정 저장
     *
     * @param requestDto 여행 PK, 사용자 식별자, 일정 입력값을 포함한 일정 추가 요청 DTO
     * @return 생성된 일정 정보를 담은 응답 DTO
     */
    @Transactional
    public AddTripScheduleResponseDto addTripSchedule(AddTripScheduleRequestDto requestDto) {
        validateAddTripScheduleRequest(requestDto);
        validateUserExistsByUsersId(requestDto.getUsersId());

        Trip trip = tripRepository.findByTripIdAndUser_UsersIdAndIsDeletedFalse(
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        validateScheduleDateInTripPeriod(requestDto.getScheduleDate(), trip.getStartDate(), trip.getEndDate());

        Place place = null;
        if (requestDto.getPlaceId() != null) {
            place = placeRepository.findByPlaceIdAndIsDeletedFalse(requestDto.getPlaceId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
        }

        int dayNo = calculateDayNo(trip.getStartDate(), requestDto.getScheduleDate());
        String normalizedMemo = normalizeTripScheduleMemo(requestDto.getMemo());

        TripSchedule savedTripSchedule = tripScheduleRepository.save(
                TripSchedule.builder()
                        .trip(trip)
                        .dayNo(dayNo)
                        .scheduleDate(requestDto.getScheduleDate())
                        .title(requestDto.getTitle().trim())
                        .address(place != null ? place.getAddress() : null)
                        .startTime(requestDto.getStartTime())
                        .endTime(requestDto.getEndTime())
                        .memo(normalizedMemo)
                        .place(place)
                        .build()
        );

        return AddTripScheduleResponseDto.from(savedTripSchedule);
    }

    /**
     * 여행 상세의 "일정 수정" 입력값을 기준으로 기존 일정을 수정합니다.
     * <p>
     * 처리 순서는 아래와 같습니다.
     * 1) 요청 필수값/형식 검증
     * 2) 사용자 존재 및 여행 소유권 검증
     * 3) 수정 대상 일정 조회
     * 4) 일정 날짜가 여행 기간 내인지 검증
     * 5) placeId 전달 시 장소 유효성 검증
     * 6) 일정 엔티티 값 갱신 후 응답 DTO 반환
     *
     * @param requestDto 여행 PK, 일정 PK, 사용자 계정, 일정 수정 입력값을 포함한 요청 DTO
     * @return 수정된 일정 상세를 담은 응답 DTO
     */
    @Transactional
    public UpdateTripScheduleResponseDto updateTripSchedule(UpdateTripScheduleRequestDto requestDto) {
        validateUpdateTripScheduleRequest(requestDto);
        validateUserExistsByUsersId(requestDto.getUsersId());

        Trip trip = tripRepository.findByTripIdAndUser_UsersIdAndIsDeletedFalse(
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        TripSchedule tripSchedule = tripScheduleRepository
                .findByTripScheduleIdAndTrip_TripIdAndTrip_User_UsersIdAndIsDeletedFalse(
                        requestDto.getTripScheduleId(),
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        validateScheduleDateInTripPeriod(requestDto.getScheduleDate(), trip.getStartDate(), trip.getEndDate());

        Place place = null;
        if (requestDto.getPlaceId() != null) {
            place = placeRepository.findByPlaceIdAndIsDeletedFalse(requestDto.getPlaceId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
        }

        int dayNo = calculateDayNo(trip.getStartDate(), requestDto.getScheduleDate());
        String normalizedMemo = normalizeTripScheduleMemo(requestDto.getMemo());

        tripSchedule.updateSchedule(
                dayNo,
                requestDto.getScheduleDate(),
                requestDto.getTitle().trim(),
                place != null ? place.getAddress() : null,
                requestDto.getStartTime(),
                requestDto.getEndTime(),
                normalizedMemo,
                place
        );

        return UpdateTripScheduleResponseDto.from(tripSchedule);
    }

    /**
     * 내 여행 상세 화면의 날짜별 일정 카드에서 선택한 장소를 여행 위시리스트에 추가합니다.
     * 현재 저장 구조는 여행 단위 위시리스트이므로 선택한 날짜는 저장하지 않고,
     * 요청 유효성 검증과 응답 문맥 정보 계산에만 사용합니다.
     *
     * @param requestDto 여행 PK, 장소 PK, 선택 날짜, 로그인 사용자 아이디를 담은 요청 DTO
     * @return 위시리스트에 추가된 장소 정보를 담은 응답 DTO
     */
    @Transactional
    public AddWishlistPlaceResponseDto addWishlistPlace(AddWishlistPlaceRequestDto requestDto) {
        validateAddWishlistPlaceRequest(requestDto);
        validateUserExistsByUsersId(requestDto.getUsersId());

        Trip trip = tripRepository.findByTripIdAndUser_UsersIdAndIsDeletedFalse(
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        Place place = placeRepository.findByPlaceIdAndIsDeletedFalse(requestDto.getPlaceId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        if (wishlistPlaceRepository.existsByTrip_TripIdAndPlace_PlaceIdAndIsDeletedFalse(
                trip.getTripId(),
                place.getPlaceId()
        )) {
            throw new BusinessException(ErrorCode.WISHLIST_PLACE_ALREADY_EXISTS);
        }

        WishlistPlace wishlistPlace = wishlistPlaceRepository.save(
                WishlistPlace.builder()
                        .trip(trip)
                        .place(place)
                        .sourceType(WishlistSourceType.RECOMMEND)
                        .build()
        );

        LocalDate responseScheduleDate = trip.getStartDate();
        int dayNo = calculateDayNo(trip.getStartDate(), responseScheduleDate);

        return AddWishlistPlaceResponseDto.from(wishlistPlace, responseScheduleDate, dayNo);
    }

    /**
     * 내 여행 상세 화면의 장소 추가하기 버튼을 눌렀을 때 표시할 저장한 장소와 위시리스트 목록을 함께 조회합니다.
     * 저장한 장소는 사용자 기준으로, 위시리스트는 현재 여행 기준으로 조회해
     * 장소 선택 화면에서 탭 형태로 바로 사용할 수 있는 응답 구조로 조합합니다.
     *
     * @param requestDto 조회 대상 여행 PK와 로그인 사용자 아이디를 담은 요청 DTO
     * @return 저장한 장소와 위시리스트 목록을 함께 담은 응답 DTO
     */
    public TripPlaceSelectionResponseDto getTripPlaceSelection(TripPlaceSelectionRequestDto requestDto) {
        validateTripPlaceSelectionRequest(requestDto);
        validateUserExistsByUsersId(requestDto.getUsersId());

        Trip trip = tripRepository.findByTripIdAndUser_UsersIdAndIsDeletedFalse(
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        List<WishlistPlace> wishlistPlaceEntities = wishlistPlaceRepository
                .findAllByTrip_TripIdAndIsDeletedFalseAndPlace_IsDeletedFalseOrderByCreatedAtDesc(trip.getTripId());

        Map<Long, Long> wishlistPlaceIdByPlaceId = createWishlistPlaceIdByPlaceIdMap(wishlistPlaceEntities);

        List<TripPlaceSelectionItemResponseDto> savedPlaces = savedPlaceRepository
                .findAllByUser_UsersIdAndIsDeletedFalseAndPlace_IsDeletedFalseOrderByCreatedAtDesc(requestDto.getUsersId())
                .stream()
                .map(savedPlace -> TripPlaceSelectionItemResponseDto.fromSavedPlace(
                        savedPlace,
                        wishlistPlaceIdByPlaceId.get(savedPlace.getPlace().getPlaceId())
                ))
                .toList();

        List<TripPlaceSelectionItemResponseDto> wishlistPlaces = wishlistPlaceEntities.stream()
                .map(TripPlaceSelectionItemResponseDto::fromWishlistPlace)
                .toList();

        return TripPlaceSelectionResponseDto.of(
                trip.getTripId(),
                trip.getTitle(),
                savedPlaces,
                wishlistPlaces
        );
    }

    /**
     * 위시리스트 엔티티 목록을 "장소 PK -> 위시리스트 PK" 맵으로 변환합니다.
     * 저장한 장소 탭에서 "이미 담긴 장소인지"를 빠르게 판단하기 위해 사용합니다.
     *
     * @param wishlistPlaces 현재 여행에 담긴 위시리스트 엔티티 목록
     * @return 장소 PK를 키로 하는 위시리스트 PK 맵
     */
    private Map<Long, Long> createWishlistPlaceIdByPlaceIdMap(List<WishlistPlace> wishlistPlaces) {
        Map<Long, Long> wishlistPlaceIdByPlaceId = new HashMap<>();
        for (WishlistPlace wishlistPlace : wishlistPlaces) {
            wishlistPlaceIdByPlaceId.put(
                    wishlistPlace.getPlace().getPlaceId(),
                    wishlistPlace.getWishlistPlaceId()
            );
        }
        return wishlistPlaceIdByPlaceId;
    }

    /**
     * 홈 화면 상단 카드에서 사용할 기준상 가장 가까운 여행 일정 정보를 조회합니다.
     * 진행 중인 여행이 있으면 해당 여행을 우선 조회하고,
     * 없으면 가장 가까운 예정 여행을 조회합니다.
     *
     * @param requestDto 조회 대상 사용자 PK를 담은 요청 DTO
     * @return 가까운 여행 일정 응답 DTO
     */
    public NearbyTripScheduleResponseDto getNearbyTripSchedule(NearbyTripScheduleRequestDto requestDto) {
        validateUserExistsByUserId(requestDto.getUserId());

        Long userId = requestDto.getUserId();
        ZonedDateTime currentDateTime = currentDateTime();
        LocalDate today = currentDateTime.toLocalDate();
        LocalTime now = currentDateTime.toLocalTime();

        return tripRepository
                .findFirstByUser_UserIdAndIsDeletedFalseAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(
                        userId,
                        today,
                        today
                )
                .map(trip -> buildNearbyTripScheduleResponse(trip, today, now))
                .or(() -> tripRepository.findFirstByUser_UserIdAndIsDeletedFalseAndStartDateAfterOrderByStartDateAsc(
                        userId,
                        today
                ).map(trip -> buildNearbyTripScheduleResponse(trip, today, now)))
                .orElseGet(NearbyTripScheduleResponseDto::empty);
    }

    /**
     * 로그인 사용자 아이디가 실제 사용자 테이블에 존재하는지 검증합니다.
     *
     * @param usersId 로그인 사용자 아이디
     */
    private void validateUserExistsByUsersId(String usersId) {
        if (!myPageRepository.existsByUsersIdAndIsDeletedFalse(usersId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }

    /**
     * 개별 일정 삭제 요청에 필요한 여행 PK, 일정 PK, 로그인 사용자 아이디가 모두 존재하는지 검증합니다.
     *
     * @param requestDto 개별 일정 삭제 요청 DTO
     */
    private void validateDeleteTripScheduleRequest(DeleteTripScheduleRequestDto requestDto) {
        if (requestDto.getTripId() == null
                || requestDto.getTripId() < 1
                || requestDto.getTripScheduleId() == null
                || requestDto.getTripScheduleId() < 1
                || requestDto.getUsersId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 위시리스트 장소 삭제 요청에 필요한 여행 PK, 위시리스트 PK, 로그인 사용자 아이디가 모두 존재하는지 검증합니다.
     *
     * @param requestDto 위시리스트 장소 삭제 요청 DTO
     */
    private void validateDeleteWishlistPlaceRequest(DeleteWishlistPlaceRequestDto requestDto) {
        if (requestDto.getTripId() == null
                || requestDto.getTripId() < 1
                || requestDto.getWishlistPlaceId() == null
                || requestDto.getWishlistPlaceId() < 1
                || requestDto.getUsersId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 내 여행 상세 조회 요청에 필요한 여행 PK와 로그인 사용자 아이디가 모두 존재하는지 검증합니다.
     *
     * @param requestDto 내 여행 상세 조회 요청 DTO
     */
    private void validateTripDetailRequest(MyTripDetailRequestDto requestDto) {
        if (requestDto.getTripId() == null || requestDto.getTripId() < 1 || requestDto.getUsersId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 내 여행 상세 일정 리스트 조회 요청에 필요한 여행 PK와 로그인 사용자 아이디를 검증합니다.
     * 사용자 식별자가 비어 있거나 여행 PK가 음수/0이면 상세 화면 구성에 필요한 조회를 수행할 수 없으므로
     * 요청 초기에 INVALID_INPUT으로 차단합니다.
     *
     * @param requestDto 내 여행 상세 일정 리스트 조회 요청 DTO
     */
    private void validateMyTripScheduleListRequest(MyTripScheduleListRequestDto requestDto) {
        if (requestDto.getTripId() == null
                || requestDto.getTripId() < 1
                || requestDto.getUsersId() == null
                || requestDto.getUsersId().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 지도용 일정 위치 조회 요청에 필요한 여행 PK와 로그인 사용자 아이디 존재 여부를 검증합니다.
     *
     * @param requestDto 일정 위치 조회 요청 DTO
     */
    private void validateTripScheduleLocationRequest(MyTripScheduleLocationRequestDto requestDto) {
        if (requestDto.getTripId() == null
                || requestDto.getTripId() < 1
                || requestDto.getUsersId() == null
                || requestDto.getUsersId().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 내 여행 상세 길찾기 요청에 필요한 여행 PK, 일정 PK, 로그인 사용자 아이디를 검증합니다.
     * 값이 누락되면 "내 여행의 특정 일정"이라는 길찾기 대상을 확정할 수 없으므로
     * 요청 초기에 INVALID_INPUT으로 차단합니다.
     *
     * @param requestDto 내 여행 상세 길찾기 요청 DTO
     */
    private void validateMyTripScheduleRouteRequest(MyTripScheduleRouteRequestDto requestDto) {
        if (requestDto.getTripId() == null
                || requestDto.getTripId() < 1
                || requestDto.getTripScheduleId() == null
                || requestDto.getTripScheduleId() < 1
                || requestDto.getUsersId() == null
                || requestDto.getUsersId().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 위시리스트 장소 추가 요청에 필요한 여행 PK, 장소 PK, 선택 날짜, 사용자 아이디 존재 여부를 검증합니다.
     *
     * @param requestDto 위시리스트 장소 추가 요청 DTO
     */
    /**
     * 방문 기록 저장 요청에 필요한 여행 PK, 일정 PK, 장소 PK, 사용자 아이디가 모두 존재하는지 검증합니다.
     *
     * @param requestDto 방문 기록 저장 요청 DTO
     */
    private void validateAddVisitedPlaceRequest(AddVisitedPlaceRequestDto requestDto) {
        if (requestDto.getTripId() == null
                || requestDto.getTripId() < 1
                || requestDto.getTripScheduleId() == null
                || requestDto.getTripScheduleId() < 1
                || requestDto.getPlaceId() == null
                || requestDto.getPlaceId() < 1
                || requestDto.getUsersId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 일정 추가 요청의 기본 입력값을 검증합니다.
     * <p>
     * 설계서 기준으로 일정명(10자), 날짜, 시작/종료 시간, 메모(100자), 선택 장소 PK 형식을 확인합니다.
     * 시간은 시작 시간이 종료 시간보다 빨라야 유효합니다.
     *
     * @param requestDto 일정 추가 요청 DTO
     */
    private void validateAddTripScheduleRequest(AddTripScheduleRequestDto requestDto) {
        if (requestDto.getTripId() == null
                || requestDto.getTripId() < 1
                || requestDto.getUsersId() == null
                || requestDto.getUsersId().isBlank()
                || requestDto.getTitle() == null
                || requestDto.getTitle().isBlank()
                || requestDto.getTitle().trim().length() > 10
                || requestDto.getScheduleDate() == null
                || requestDto.getStartTime() == null
                || requestDto.getEndTime() == null
                || !requestDto.getStartTime().isBefore(requestDto.getEndTime())
                || (requestDto.getPlaceId() != null && requestDto.getPlaceId() < 1)
                || (requestDto.getMemo() != null && requestDto.getMemo().length() > 100)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 일정 수정 요청의 기본 입력값을 검증합니다.
     * <p>
     * 화면/기획 기준으로 일정명, 날짜, 시작/종료 시간은 필수이며
     * 일정명(10자), 메모(100자), placeId 형식(양수) 제약을 함께 확인합니다.
     *
     * @param requestDto 일정 수정 요청 DTO
     */
    private void validateUpdateTripScheduleRequest(UpdateTripScheduleRequestDto requestDto) {
        if (requestDto.getTripId() == null
                || requestDto.getTripId() < 1
                || requestDto.getTripScheduleId() == null
                || requestDto.getTripScheduleId() < 1
                || requestDto.getUsersId() == null
                || requestDto.getUsersId().isBlank()
                || requestDto.getTitle() == null
                || requestDto.getTitle().isBlank()
                || requestDto.getTitle().trim().length() > 10
                || requestDto.getScheduleDate() == null
                || requestDto.getStartTime() == null
                || requestDto.getEndTime() == null
                || !requestDto.getStartTime().isBefore(requestDto.getEndTime())
                || (requestDto.getPlaceId() != null && requestDto.getPlaceId() < 1)
                || (requestDto.getMemo() != null && requestDto.getMemo().length() > 100)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 입력받은 일정 날짜가 여행 기간 내에 포함되는지 검증합니다.
     *
     * @param scheduleDate 일정 날짜
     * @param tripStartDate 여행 시작일
     * @param tripEndDate 여행 종료일
     */
    private void validateScheduleDateInTripPeriod(LocalDate scheduleDate, LocalDate tripStartDate, LocalDate tripEndDate) {
        if (scheduleDate.isBefore(tripStartDate) || scheduleDate.isAfter(tripEndDate)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private void validateAddWishlistPlaceRequest(AddWishlistPlaceRequestDto requestDto) {
        if (requestDto.getTripId() == null
                || requestDto.getTripId() < 1
                || requestDto.getPlaceId() == null
                || requestDto.getPlaceId() < 1
                || requestDto.getUsersId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 사용자 PK가 실제 사용자 테이블에 존재하는지 검증합니다.
     *
     * @param userId 로그인 사용자 PK
     */
    /**
     * 저장한 장소/위시리스트 조회 요청에 필요한 여행 PK와 사용자 아이디 존재 여부를 검증합니다.
     *
     * @param requestDto 저장한 장소/위시리스트 조회 요청 DTO
     */
    private void validateTripPlaceSelectionRequest(TripPlaceSelectionRequestDto requestDto) {
        if (requestDto.getTripId() == null
                || requestDto.getTripId() < 1
                || requestDto.getUsersId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private void validateUserExistsByUserId(Long userId) {
        if (!myPageRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }

    /**
     * 로그인 사용자 아이디로 사용자 엔티티를 조회합니다.
     *
     * @param usersId 로그인 사용자 아이디
     * @return 여행 생성 주체가 되는 사용자 엔티티
     */
    private User findUserByUsersId(String usersId) {
        return myPageRepository.findByUsersIdAndIsDeletedFalse(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 일정 진행 여부와 여행 상태 계산에 사용할 현재 시각을 애플리케이션 기준 시간대(Asia/Seoul)로 고정합니다.
     * 서버 JVM 기본 시간대와 무관하게 동일한 결과를 반환하도록 공통 진입점을 둡니다.
     *
     * @return 애플리케이션 기준 현재 일시
     */
    private ZonedDateTime currentDateTime() {
        return ZonedDateTime.now(APP_ZONE_ID);
    }

    private LocalDate currentDate() {
        return currentDateTime().toLocalDate();
    }

    /**
     * 여행 추가 요청의 시작일과 종료일이 화면 기획 조건에 맞는지 검증합니다.
     * 시작일이 오늘보다 이전일 수 없고, 종료일은 시작일보다 빠를 수 없습니다.
     *
     * @param startDate 여행 시작일
     * @param endDate 여행 종료일
     */
    private void validateCreateTripDates(LocalDate startDate, LocalDate endDate) {
        LocalDate today = currentDate();

        if (startDate.isBefore(today) || endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 여행 수정 시에는 과거 시작일도 허용하므로 시작일과 종료일의 순서만 검증합니다.
     *
     * @param startDate 수정할 여행 시작일
     * @param endDate 수정할 여행 종료일
     */
    private void validateUpdateTripDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 여행 공유 요청에 필요한 여행 PK와 사용자 아이디가 모두 정상인지 검증합니다.
     *
     * @param requestDto 여행 공유 요청 DTO
     */
    private void validateShareTripRequest(ShareTripRequestDto requestDto) {
        if (requestDto.getTripId() == null || requestDto.getTripId() < 1 || requestDto.getUsersId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 여행에 포함된 일정이 수정된 여행 기간 안에 모두 포함되는지 확인합니다.
     * 등록된 일정이 새로운 여행 범위를 벗어나면 상세 화면의 일정 목록과 여행 기간이 어긋나므로 수정 요청을 차단합니다.
     *
     * @param schedules 수정 대상 여행에 연결된 전체 일정 목록
     * @param startDate 수정할 여행 시작일
     * @param endDate 수정할 여행 종료일
     */
    private void validateScheduleDatesWithinTripRange(List<TripSchedule> schedules, LocalDate startDate, LocalDate endDate) {
        boolean hasScheduleOutsideRange = schedules.stream()
                .anyMatch(schedule -> schedule.getScheduleDate().isBefore(startDate)
                        || schedule.getScheduleDate().isAfter(endDate));

        if (hasScheduleOutsideRange) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 여행 시작일이 바뀐 뒤에는 일정 상세 화면에서 일차 정보가 올바르게 보이도록 모든 일정의 dayNo를 다시 계산합니다.
     *
     * @param schedules 수정 대상 여행에 연결된 일정 목록
     * @param tripStartDate 수정된 여행 시작일
     */
    private void refreshTripScheduleDayNumbers(List<TripSchedule> schedules, LocalDate tripStartDate) {
        schedules.forEach(schedule -> schedule.updateDayNo(calculateDayNo(tripStartDate, schedule.getScheduleDate())));
    }

    /**
     * 빈 문자열로 들어온 이미지 URL은 null로 정규화합니다.
     *
     * @param imageUrl 화면에서 전달된 여행 이미지 URL
     * @return 저장 가능한 여행 이미지 URL
     */
    private String normalizeImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        return imageUrl.trim();
    }

    /**
     * 일정 메모 입력값을 저장 가능한 형태로 정규화합니다.
     * 메모가 비어 있으면 null로 저장하여 불필요한 공백 문자열 누적을 방지합니다.
     *
     * @param memo 일정 메모 입력값
     * @return 정규화된 메모 값(null 허용)
     */
    private String normalizeTripScheduleMemo(String memo) {
        if (memo == null || memo.isBlank()) {
            return null;
        }
        return memo.trim();
    }

    /**
     * 여행 카드 목록 응답을 공통 규칙으로 생성합니다.
     * 여행 상태를 현재 날짜 기준으로 계산하고, 필터 조건에 맞는 카드만 반환합니다.
     *
     * @param usersId 로그인 사용자 아이디
     * @param filterType 적용할 여행 필터 유형
     * @return 필터 조건이 반영된 여행 카드 목록 응답 DTO
     */
    private MyTripListResponseDto getMyTripListResponse(String usersId, MyTripFilterType filterType) {
        validateUserExistsByUsersId(usersId);

        List<Trip> trips = tripRepository.findAllByUser_UsersIdAndIsDeletedFalse(usersId);
        if (trips.isEmpty()) {
            return MyTripListResponseDto.of(List.of());
        }

        Map<Long, Integer> scheduleCountMap = createScheduleCountMap(trips);
        LocalDate today = currentDate();

        List<MyTripSummaryResponseDto> tripResponses = trips.stream()
                .map(trip -> MyTripSummaryResponseDto.of(
                        trip,
                        resolveTripStatus(trip, today),
                        scheduleCountMap.getOrDefault(trip.getTripId(), 0)
                ))
                .filter(tripResponse -> matchesTripFilter(tripResponse.getTripStatus(), filterType))
                .sorted(buildMyTripSummaryComparator())
                .toList();

        return MyTripListResponseDto.of(tripResponses);
    }

    /**
     * 여행 카드 목록에 필요한 일정 개수를 여행 PK 기준으로 집계합니다.
     *
     * @param trips 조회 대상 여행 엔티티 목록
     * @return 여행 PK를 키로 가지는 일정 개수 맵
     */
    private Map<Long, Integer> createScheduleCountMap(List<Trip> trips) {
        List<Long> tripIds = trips.stream()
                .map(Trip::getTripId)
                .toList();

        return tripScheduleRepository.findAllByTrip_TripIdInAndIsDeletedFalse(tripIds)
                .stream()
                .collect(Collectors.groupingBy(
                        tripSchedule -> tripSchedule.getTrip().getTripId(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    /**
     * 여행 시작일부터 종료일까지 모든 날짜를 순회하며 일차별 일정 섹션 목록을 생성합니다.
     * 일정이 없는 날짜도 빈 리스트를 포함해 상세 화면의 일차 영역이 비지 않도록 합니다.
     *
     * @param trip 조회 대상 여행 엔티티
     * @param schedulesByDate 날짜별 일정 목록 맵
     * @param tripDayCount 여행 총 일수
     * @return 일차별 일정 섹션 응답 목록
     */
    private List<MyTripDailyScheduleResponseDto> buildDailyScheduleSections(
            Trip trip,
            Map<LocalDate, List<MyTripScheduleItemResponseDto>> schedulesByDate,
            long tripDayCount
    ) {
        return IntStream.range(0, Math.toIntExact(tripDayCount))
                .mapToObj(dayOffset -> {
                    LocalDate scheduleDate = trip.getStartDate().plusDays(dayOffset);
                    List<MyTripScheduleItemResponseDto> schedules =
                            schedulesByDate.getOrDefault(scheduleDate, List.of());

                    return MyTripDailyScheduleResponseDto.of(
                            calculateDayNo(trip.getStartDate(), scheduleDate),
                            scheduleDate,
                            schedules
                    );
                })
                .toList();
    }

    private String resolveTripDetailImageUrl(Trip trip, List<TripSchedule> tripSchedules) {
        Map<Long, ScheduleLocationPlaceSnapshot> placeSnapshots = resolveScheduleLocationPlaceSnapshots(tripSchedules);

        for (TripSchedule tripSchedule : tripSchedules) {
            Place place = tripSchedule.getPlace();
            if (place == null || place.getPlaceId() == null) {
                continue;
            }

            ScheduleLocationPlaceSnapshot placeSnapshot = placeSnapshots.get(place.getPlaceId());
            if (placeSnapshot != null && hasText(placeSnapshot.imageUrl())) {
                return placeSnapshot.imageUrl();
            }
        }

        return normalizeImageUrl(trip.getImageUrl());
    }

    /**
     * 내 여행 상세 일정 리스트 API 전용으로 여행 기간 전체의 일차별 섹션을 구성합니다.
     * 기존 상세 API와 동일하게 여행 시작일부터 종료일까지 모든 날짜를 순회하며,
     * 일정이 없는 날짜도 빈 schedules 배열을 내려 화면 구조를 고정합니다.
     *
     * @param trip 조회 대상 여행 엔티티
     * @param schedulesByDate 날짜별 여행 일정 엔티티 목록 맵
     * @param tripDayCount 여행 총 일수
     * @param today 서비스 기준 현재 날짜
     * @param now 서비스 기준 현재 시간
     * @param visitedPlaceIds 이미 방문 기록으로 저장된 장소 PK 집합
     * @return 내 여행 상세 일정 리스트용 일차별 섹션 목록
     */
    private List<MyTripScheduleListResponseDto.DayScheduleResponseDto> buildTripScheduleListDailySections(
            Trip trip,
            Map<LocalDate, List<TripSchedule>> schedulesByDate,
            long tripDayCount,
            LocalDate today,
            LocalTime now,
            Set<Long> visitedPlaceIds
    ) {
        return IntStream.range(0, Math.toIntExact(tripDayCount))
                .mapToObj(dayOffset -> {
                    LocalDate scheduleDate = trip.getStartDate().plusDays(dayOffset);
                    List<TripSchedule> schedules = schedulesByDate.getOrDefault(scheduleDate, List.of());

                    List<MyTripScheduleListResponseDto.ScheduleItemResponseDto> scheduleItems =
                            IntStream.range(0, schedules.size())
                                    .mapToObj(index -> toTripScheduleListItemResponse(
                                            schedules.get(index),
                                            index + 1,
                                            today,
                                            now,
                                            visitedPlaceIds
                                    ))
                                    .toList();

                    return MyTripScheduleListResponseDto.DayScheduleResponseDto.of(
                            calculateDayNo(trip.getStartDate(), scheduleDate),
                            scheduleDate,
                            scheduleItems
                    );
                })
                .toList();
    }

    /**
     * 여행 일정 엔티티를 내 여행 상세 일정 리스트 API의 일정 카드 DTO로 변환합니다.
     * 화면 렌더링에 필요한 일정 순번, 현재 진행 상태, 방문 기록 여부를 함께 계산해 반환합니다.
     *
     * @param tripSchedule 변환 대상 여행 일정 엔티티
     * @param scheduleOrder 해당 일차 내 일정 순번(1부터 시작)
     * @param today 서비스 기준 현재 날짜
     * @param now 서비스 기준 현재 시간
     * @param visitedPlaceIds 이미 방문 기록으로 저장된 장소 PK 집합
     * @return 내 여행 상세 일정 리스트 API용 일정 카드 DTO
     */
    private MyTripScheduleListResponseDto.ScheduleItemResponseDto toTripScheduleListItemResponse(
            TripSchedule tripSchedule,
            int scheduleOrder,
            LocalDate today,
            LocalTime now,
            Set<Long> visitedPlaceIds
    ) {
        Long placeId = tripSchedule.getPlace() != null ? tripSchedule.getPlace().getPlaceId() : null;
        boolean isOngoing = isCurrentTripSchedule(tripSchedule, today, now);
        boolean visited = placeId != null && visitedPlaceIds.contains(placeId);
        boolean canAddVisitedPlace = placeId != null && isOngoing && !visited;
        String address = resolveScheduleAddress(tripSchedule);

        return MyTripScheduleListResponseDto.ScheduleItemResponseDto.from(
                tripSchedule,
                scheduleOrder,
                address,
                isOngoing,
                visited,
                canAddVisitedPlace
        );
    }

    /**
     * 선택 가능한 날짜 드롭다운 목록을 여행 시작일부터 종료일까지 생성합니다.
     *
     * @param trip 조회 대상 여행 엔티티
     * @return 날짜 드롭다운 옵션 목록
     */
    /**
     * 여행에 이미 저장된 방문 기록 장소 PK 집합을 만들어 일정 카드 상태 계산에 재사용합니다.
     * 일정마다 개별 조회를 반복하지 않도록 여행 단위로 한 번만 방문 기록을 읽어옵니다.
     *
     * @param tripId 방문 기록 조회 대상 여행 PK
     * @return 해당 여행에서 이미 방문 기록으로 저장된 장소 PK 집합
     */
    private Set<Long> createVisitedPlaceIdSet(Long tripId) {
        return visitedPlaceRepository.findAllByTrip_TripIdAndIsDeletedFalse(tripId).stream()
                .map(visitedPlace -> visitedPlace.getPlace().getPlaceId())
                .collect(Collectors.toSet());
    }

    /**
     * 지도 보기에서 바로 사용할 수 있는 좌표 보유 일정 개수를 계산합니다.
     * 화면에서 지도 보기 버튼을 활성화할지 판단할 때 함께 사용합니다.
     *
     * @param tripSchedules 여행에 연결된 전체 일정 목록
     * @return 좌표를 보유한 일정 개수
     */
    private int countLocationSchedules(List<TripSchedule> tripSchedules) {
        return (int) tripSchedules.stream()
                .filter(this::hasLocation)
                .count();
    }

    /**
     * 현재 시점과 겹치는 일정이 있으면 화면 상단에 바로 사용할 수 있도록 요약 응답을 생성합니다.
     * 현재 일정이 없으면 null을 반환하여 화면에서 고정 액션 영역을 숨길 수 있게 합니다.
     *
     * @param tripSchedules 여행에 속한 전체 일정 목록
     * @param today 서비스 기준 현재 날짜
     * @param now 서비스 기준 현재 시간
     * @param visitedPlaceIds 이미 방문 기록으로 저장된 장소 PK 집합
     * @return 현재 진행 중 일정 요약 응답 DTO, 없으면 null
     */
    private MyTripCurrentScheduleResponseDto findCurrentSchedule(
            List<TripSchedule> tripSchedules,
            LocalDate today,
            LocalTime now,
            Set<Long> visitedPlaceIds
    ) {
        return tripSchedules.stream()
                .filter(tripSchedule -> isCurrentTripSchedule(tripSchedule, today, now))
                .findFirst()
                .map(tripSchedule -> toCurrentScheduleResponse(tripSchedule, visitedPlaceIds))
                .orElse(null);
    }

    /**
     * 현재 진행 중 일정 엔티티를 화면 상단 요약용 DTO로 변환합니다.
     * 방문지 저장과 길찾기 버튼 활성화 여부도 함께 계산해 프론트엔드가 즉시 사용할 수 있도록 합니다.
     *
     * @param tripSchedule 현재 진행 중 일정 엔티티
     * @param visitedPlaceIds 이미 방문 기록으로 저장된 장소 PK 집합
     * @return 현재 진행 중 일정 요약 응답 DTO
     */
    private MyTripCurrentScheduleResponseDto toCurrentScheduleResponse(
            TripSchedule tripSchedule,
            Set<Long> visitedPlaceIds
    ) {
        Long placeId = tripSchedule.getPlace() != null ? tripSchedule.getPlace().getPlaceId() : null;
        boolean visited = placeId != null && visitedPlaceIds.contains(placeId);
        boolean canAddVisitedPlace = placeId != null && !visited;
        String address = resolveScheduleAddress(tripSchedule);
        boolean canSearchRoute = canSearchRoute(tripSchedule, address);

        return MyTripCurrentScheduleResponseDto.of(
                tripSchedule,
                address,
                visited,
                canAddVisitedPlace,
                canSearchRoute
        );
    }

    /**
     * 여행 일정 엔티티를 일정 상세 화면용 카드 응답 DTO로 변환합니다.
     * 현재 시각 기준 진행 중 여부와 상세 화면 액션 상태를 함께 계산합니다.
     *
     * @param tripSchedule 변환 대상 여행 일정 엔티티
     * @param today 서비스 기준 현재 날짜
     * @param now 서비스 기준 현재 시간
     * @param visitedPlaceIds 이미 방문 기록으로 저장된 장소 PK 집합
     * @return 화면에 바로 사용할 수 있는 일정 카드 응답 DTO
     */
    private MyTripScheduleItemResponseDto toTripScheduleItemResponse(
            TripSchedule tripSchedule,
            LocalDate today,
            LocalTime now,
            Set<Long> visitedPlaceIds
    ) {
        Long placeId = tripSchedule.getPlace() != null ? tripSchedule.getPlace().getPlaceId() : null;
        boolean isCurrent = isCurrentTripSchedule(tripSchedule, today, now);
        boolean visited = placeId != null && visitedPlaceIds.contains(placeId);
        boolean canAddVisitedPlace = placeId != null && isCurrent && !visited;
        String address = resolveScheduleAddress(tripSchedule);
        boolean canSearchRoute = canSearchRoute(tripSchedule, address);
        boolean canEditSchedule = canEditTripSchedule(tripSchedule);

        return MyTripScheduleItemResponseDto.from(
                tripSchedule,
                address,
                isCurrent,
                visited,
                canAddVisitedPlace,
                canSearchRoute,
                canEditSchedule
        );
    }

    /**
     * 지도 페이지에서 사용할 일정 위치 항목 목록을 일정 순서대로 생성합니다.
     * 좌표가 있는 일정에만 핀 순서를 부여하여 프론트엔드가 번호 핀과 이동 동선을 바로 그릴 수 있도록 합니다.
     *
     * @param tripSchedules 여행에 속한 전체 일정 목록
     * @param today 서비스 기준 현재 날짜
     * @param now 서비스 기준 현재 시간
     * @param visitedPlaceIds 이미 방문 인증된 장소 PK 집합
     * @return 지도 페이지용 일정 위치 항목 목록
     */
    private List<MyTripScheduleLocationItemResponseDto> buildTripScheduleLocationItems(
            List<TripSchedule> tripSchedules,
            LocalDate today,
            LocalTime now,
            Set<Long> visitedPlaceIds
    ) {
        List<MyTripScheduleLocationItemResponseDto> responses = new ArrayList<>(tripSchedules.size());
        List<Integer> pinOrders = TripScheduleLocationPinOrderResolver.resolve(tripSchedules);
        Map<Long, ScheduleLocationPlaceSnapshot> placeSnapshots = resolveScheduleLocationPlaceSnapshots(tripSchedules);

        for (int scheduleIndex = 0; scheduleIndex < tripSchedules.size(); scheduleIndex++) {
            TripSchedule tripSchedule = tripSchedules.get(scheduleIndex);
            Integer currentPinOrder = pinOrders.get(scheduleIndex);
            Long placeId = tripSchedule.getPlace() != null ? tripSchedule.getPlace().getPlaceId() : null;
            ScheduleLocationPlaceSnapshot placeSnapshot =
                    placeId != null ? placeSnapshots.get(placeId) : null;

            responses.add(toTripScheduleLocationItemResponse(
                    tripSchedule,
                    scheduleIndex + 1,
                    currentPinOrder,
                    placeSnapshot,
                    today,
                    now,
                    visitedPlaceIds
            ));
        }

        return responses;
    }

    /**
     * 여행 일정 엔티티를 지도 페이지 전용 위치 항목 DTO로 변환합니다.
     * 기존 일정 카드 계산 로직과 동일하게 현재 진행 중 여부와 방문 인증 가능 여부를 함께 계산합니다.
     *
     * @param tripSchedule 변환 대상 여행 일정 엔티티
     * @param scheduleOrder 전체 일정 순서
     * @param pinOrder 지도 핀 순서, 좌표가 없으면 null
     * @param today 서비스 기준 현재 날짜
     * @param now 서비스 기준 현재 시간
     * @param visitedPlaceIds 이미 방문 인증된 장소 PK 집합
     * @return 지도 페이지에서 사용할 일정 위치 항목 DTO
     */
    private MyTripScheduleLocationItemResponseDto toTripScheduleLocationItemResponse(
            TripSchedule tripSchedule,
            int scheduleOrder,
            Integer pinOrder,
            ScheduleLocationPlaceSnapshot placeSnapshot,
            LocalDate today,
            LocalTime now,
            Set<Long> visitedPlaceIds
    ) {
        Long placeId = tripSchedule.getPlace() != null ? tripSchedule.getPlace().getPlaceId() : null;
        boolean isCurrent = isCurrentTripSchedule(tripSchedule, today, now);
        boolean visited = placeId != null && visitedPlaceIds.contains(placeId);
        boolean canAddVisitedPlace = placeId != null && isCurrent && !visited;

        return MyTripScheduleLocationItemResponseDto.from(
                tripSchedule,
                scheduleOrder,
                pinOrder,
                placeSnapshot != null ? placeSnapshot.placeName() : null,
                resolveScheduleLocationAddress(tripSchedule, placeSnapshot),
                placeSnapshot != null ? placeSnapshot.description() : null,
                placeSnapshot != null ? placeSnapshot.imageUrl() : null,
                isCurrent,
                visited,
                canAddVisitedPlace
        );
    }

    /**
     * 일정 날짜와 시작/종료 시간이 현재 시각과 겹치는지 확인합니다.
     * 같은 날짜이면서 현재 시간이 시작 시간과 종료 시간 사이에 포함되면 진행 중 일정으로 판단합니다.
     *
     * @param tripSchedule 판별 대상 일정 엔티티
     * @param today 서비스 기준 현재 날짜
     * @param now 서비스 기준 현재 시간
     * @return 현재 진행 중 일정이면 true
     */
    private boolean isCurrentTripSchedule(TripSchedule tripSchedule, LocalDate today, LocalTime now) {
        return tripSchedule.getScheduleDate().isEqual(today)
                && !tripSchedule.getStartTime().isAfter(now)
                && !tripSchedule.getEndTime().isBefore(now);
    }

    /**
     * 일정에 직접 입력된 주소가 있으면 우선 사용하고, 없으면 연결된 장소의 기본 주소로 보완합니다.
     * 상세 화면 카드와 현재 일정 요약에서 동일한 주소 기준을 사용하기 위해 공통 메서드로 분리합니다.
     *
     * @param tripSchedule 주소를 해석할 일정 엔티티
     * @return 화면 노출과 길찾기 판단에 사용할 기준 주소
     */
    private String resolveScheduleAddress(TripSchedule tripSchedule) {
        if (hasText(tripSchedule.getAddress())) {
            return tripSchedule.getAddress().trim();
        }

        if (tripSchedule.getPlace() != null && hasText(tripSchedule.getPlace().getAddress())) {
            return tripSchedule.getPlace().getAddress().trim();
        }

        return null;
    }

    private Map<Long, ScheduleLocationPlaceSnapshot> resolveScheduleLocationPlaceSnapshots(List<TripSchedule> tripSchedules) {
        Map<Long, ScheduleLocationPlaceSnapshot> snapshotsByPlaceId = new HashMap<>();
        Map<String, ScheduleLocationPlaceSnapshot> snapshotsByGooglePlaceId = new HashMap<>();

        for (TripSchedule tripSchedule : tripSchedules) {
            Place place = tripSchedule.getPlace();
            if (place == null || place.getPlaceId() == null || snapshotsByPlaceId.containsKey(place.getPlaceId())) {
                continue;
            }

            ScheduleLocationPlaceSnapshot snapshot = null;
            String googlePlaceId = nullableTrim(place.getGooglePlaceId());

            if (hasText(googlePlaceId)) {
                snapshot = snapshotsByGooglePlaceId.computeIfAbsent(
                        googlePlaceId,
                        ignored -> resolveScheduleLocationPlaceSnapshot(place)
                );
            } else {
                snapshot = resolveScheduleLocationPlaceSnapshot(place);
            }

            snapshotsByPlaceId.put(
                    place.getPlaceId(),
                    snapshot != null ? snapshot : resolveFallbackScheduleLocationPlaceSnapshot(place)
            );
        }

        return snapshotsByPlaceId;
    }

    private ScheduleLocationPlaceSnapshot resolveScheduleLocationPlaceSnapshot(Place place) {
        GooglePlaceSearchService.GooglePlaceCandidate googleCandidate = resolveScheduleLocationGoogleCandidate(place);
        if (googleCandidate == null) {
            return resolveFallbackScheduleLocationPlaceSnapshot(place);
        }

        ScheduleLocationPlaceSnapshot snapshot = new ScheduleLocationPlaceSnapshot(
                firstNonBlank(
                        truncate(nullableTrim(googleCandidate.name()), PLACE_NAME_MAX_LENGTH),
                        truncate(nullableTrim(place.getName()), PLACE_NAME_MAX_LENGTH)
                ),
                firstNonBlank(
                        truncate(nullableTrim(googleCandidate.shortFormattedAddress()), ADDRESS_MAX_LENGTH),
                        truncate(nullableTrim(googleCandidate.formattedAddress()), ADDRESS_MAX_LENGTH),
                        truncate(nullableTrim(place.getAddress()), ADDRESS_MAX_LENGTH)
                ),
                firstNonBlank(
                        truncate(nullableTrim(googleCandidate.editorialSummary()), DESCRIPTION_MAX_LENGTH),
                        truncate(nullableTrim(place.getDescription()), DESCRIPTION_MAX_LENGTH)
                ),
                resolveScheduleLocationImageUrl(place, googleCandidate.firstPhotoName())
        );

        backfillScheduleLocationPlaceFromGoogle(place, googleCandidate, snapshot);
        return snapshot;
    }

    private ScheduleLocationPlaceSnapshot resolveFallbackScheduleLocationPlaceSnapshot(Place place) {
        return new ScheduleLocationPlaceSnapshot(
                truncate(nullableTrim(place.getName()), PLACE_NAME_MAX_LENGTH),
                truncate(nullableTrim(place.getAddress()), ADDRESS_MAX_LENGTH),
                truncate(nullableTrim(place.getDescription()), DESCRIPTION_MAX_LENGTH),
                resolveStoredScheduleLocationImageUrl(place)
        );
    }

    private String resolveScheduleLocationImageUrl(Place place, String photoName) {
        if (hasText(photoName)) {
            String photoUri = googlePlaceSearchService.getPhotoUri(photoName);
            if (hasText(photoUri)) {
                return truncate(photoUri, IMAGE_URL_MAX_LENGTH);
            }
        }

        return resolveStoredScheduleLocationImageUrl(place);
    }

    private String resolveScheduleLocationAddress(
            TripSchedule tripSchedule,
            ScheduleLocationPlaceSnapshot placeSnapshot
    ) {
        if (placeSnapshot != null && hasText(placeSnapshot.address())) {
            return placeSnapshot.address();
        }

        return resolveScheduleAddress(tripSchedule);
    }

    private GooglePlaceSearchService.GooglePlaceCandidate resolveScheduleLocationGoogleCandidate(Place place) {
        GooglePlaceSearchService.GooglePlaceCandidate detailsCandidate = resolveScheduleLocationDetailsCandidate(place);
        if (detailsCandidate != null) {
            return detailsCandidate;
        }

        GooglePlaceSearchService.GooglePlaceCandidate nearbyCandidate = resolveScheduleLocationNearbyCandidate(place);
        if (nearbyCandidate != null) {
            if (hasText(nearbyCandidate.googlePlaceId())) {
                GooglePlaceSearchService.GooglePlaceCandidate details =
                        googlePlaceSearchService.getPlaceDetails(nearbyCandidate.googlePlaceId());
                if (details != null) {
                    return mergeScheduleLocationCandidates(nearbyCandidate, details);
                }
            }
            return nearbyCandidate;
        }

        String searchQuery = buildScheduleLocationPlaceSearchQuery(place);
        if (!hasText(searchQuery)) {
            return null;
        }

        List<GooglePlaceSearchService.GooglePlaceCandidate> candidates = googlePlaceSearchService.searchPlaces(searchQuery);
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        GooglePlaceSearchService.GooglePlaceCandidate bestCandidate = selectBestScheduleLocationCandidate(
                place,
                candidates,
                true
        );
        if (bestCandidate == null) {
            return null;
        }

        if (hasText(bestCandidate.googlePlaceId())) {
            GooglePlaceSearchService.GooglePlaceCandidate details =
                    googlePlaceSearchService.getPlaceDetails(bestCandidate.googlePlaceId());
            if (details != null) {
                return mergeScheduleLocationCandidates(bestCandidate, details);
            }
        }

        return bestCandidate;
    }

    private GooglePlaceSearchService.GooglePlaceCandidate resolveScheduleLocationDetailsCandidate(Place place) {
        if (!hasText(place.getGooglePlaceId())) {
            return null;
        }

        return googlePlaceSearchService.getPlaceDetails(place.getGooglePlaceId());
    }

    private GooglePlaceSearchService.GooglePlaceCandidate resolveScheduleLocationNearbyCandidate(Place place) {
        if (place.getLatitude() == null || place.getLongitude() == null) {
            return null;
        }

        List<GooglePlaceSearchService.GooglePlaceCandidate> candidates = googlePlaceSearchService.searchNearbyPlaces(
                place.getLatitude().doubleValue(),
                place.getLongitude().doubleValue(),
                SCHEDULE_LOCATION_NEARBY_SEARCH_RADIUS_METERS,
                List.of(resolveGoogleIncludedType(place.getPlaceType())),
                SCHEDULE_LOCATION_NEARBY_RESULT_COUNT
        );

        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        return selectBestScheduleLocationCandidate(place, candidates, !hasText(place.getGooglePlaceId()));
    }

    private String buildScheduleLocationPlaceSearchQuery(Place place) {
        String name = nullableTrim(place.getName());
        String address = nullableTrim(place.getAddress());
        String cityName = nullableTrim(place.getCityName());
        String countryName = nullableTrim(place.getCountryName());

        StringBuilder queryBuilder = new StringBuilder();
        appendScheduleLocationSearchToken(queryBuilder, name);
        appendScheduleLocationSearchToken(queryBuilder, address);
        appendScheduleLocationSearchToken(queryBuilder, cityName);
        appendScheduleLocationSearchToken(queryBuilder, countryName);
        return nullableTrim(queryBuilder.toString());
    }

    private GooglePlaceSearchService.GooglePlaceCandidate selectBestScheduleLocationCandidate(
            Place place,
            List<GooglePlaceSearchService.GooglePlaceCandidate> candidates,
            boolean requireStrongNameMatch
    ) {
        String targetName = normalizeMatchingText(place.getName());
        String targetAddress = normalizeMatchingText(place.getAddress());

        List<GooglePlaceSearchService.GooglePlaceCandidate> filteredCandidates = candidates.stream()
                .filter(candidate -> candidate != null)
                .filter(candidate -> !requireStrongNameMatch || hasStrongScheduleLocationNameMatch(targetName, candidate))
                .toList();

        if (filteredCandidates.isEmpty()) {
            return null;
        }

        return filteredCandidates.stream()
                .max((left, right) -> Integer.compare(
                        calculateScheduleLocationCandidateScore(place, right, targetName, targetAddress),
                        calculateScheduleLocationCandidateScore(place, left, targetName, targetAddress)
                ))
                .orElse(null);
    }

    private boolean hasStrongScheduleLocationNameMatch(
            String targetName,
            GooglePlaceSearchService.GooglePlaceCandidate candidate
    ) {
        String candidateName = normalizeMatchingText(candidate.name());
        if (!hasText(targetName) || !hasText(candidateName)) {
            return false;
        }
        if (targetName.equals(candidateName)) {
            return true;
        }

        int nameDistance = calculateLevenshteinDistance(targetName, candidateName);
        return nameDistance <= 2
                || candidateName.contains(targetName)
                || targetName.contains(candidateName);
    }

    private int calculateScheduleLocationCandidateScore(
            Place place,
            GooglePlaceSearchService.GooglePlaceCandidate candidate,
            String targetName,
            String targetAddress
    ) {
        int score = 0;
        String candidateName = normalizeMatchingText(candidate.name());
        String candidateShortAddress = normalizeMatchingText(candidate.shortFormattedAddress());
        String candidateFormattedAddress = normalizeMatchingText(candidate.formattedAddress());

        if (hasText(targetName) && targetName.equals(candidateName)) {
            score += 100;
        } else if (hasText(targetName) && hasText(candidateName)) {
            int nameDistance = calculateLevenshteinDistance(targetName, candidateName);
            if (nameDistance <= 1) {
                score += 90;
            } else if (nameDistance <= 2) {
                score += 75;
            } else if (candidateName.contains(targetName) || targetName.contains(candidateName)) {
                score += 60;
            }
        }

        if (hasText(targetAddress) && hasText(candidateShortAddress) && targetAddress.contains(candidateShortAddress)) {
            score += 40;
        } else if (hasText(targetAddress)
                && hasText(candidateFormattedAddress)
                && (targetAddress.contains(candidateFormattedAddress) || candidateFormattedAddress.contains(targetAddress))) {
            score += 30;
        }

        if (place.getLatitude() != null
                && place.getLongitude() != null
                && candidate.latitude() != null
                && candidate.longitude() != null) {
            double distanceMeters = calculateDistanceMeters(
                    place.getLatitude().doubleValue(),
                    place.getLongitude().doubleValue(),
                    candidate.latitude(),
                    candidate.longitude()
            );

            if (distanceMeters <= 50) {
                score += 40;
            } else if (distanceMeters <= 150) {
                score += 25;
            } else if (distanceMeters <= 300) {
                score += 10;
            }
        }

        return score;
    }

    private void backfillScheduleLocationPlaceFromGoogle(
            Place place,
            GooglePlaceSearchService.GooglePlaceCandidate googleCandidate,
            ScheduleLocationPlaceSnapshot snapshot
    ) {
        if (hasText(place.getGooglePlaceId()) || !hasText(googleCandidate.googlePlaceId())) {
            return;
        }

        if (!hasStrongScheduleLocationNameMatch(normalizeMatchingText(place.getName()), googleCandidate)) {
            return;
        }

        place.backfillGoogleReference(
                googleCandidate.googlePlaceId(),
                snapshot.placeName(),
                snapshot.address(),
                googleCandidate.latitude() != null ? BigDecimal.valueOf(googleCandidate.latitude()) : null,
                googleCandidate.longitude() != null ? BigDecimal.valueOf(googleCandidate.longitude()) : null,
                snapshot.description(),
                snapshot.imageUrl()
        );
    }

    private GooglePlaceSearchService.GooglePlaceCandidate mergeScheduleLocationCandidates(
            GooglePlaceSearchService.GooglePlaceCandidate baseCandidate,
            GooglePlaceSearchService.GooglePlaceCandidate detailsCandidate
    ) {
        return new GooglePlaceSearchService.GooglePlaceCandidate(
                firstNonBlank(detailsCandidate.googlePlaceId(), baseCandidate.googlePlaceId()),
                firstNonBlank(detailsCandidate.name(), baseCandidate.name()),
                firstNonBlank(detailsCandidate.formattedAddress(), baseCandidate.formattedAddress()),
                firstNonBlank(detailsCandidate.shortFormattedAddress(), baseCandidate.shortFormattedAddress()),
                detailsCandidate.latitude() != null ? detailsCandidate.latitude() : baseCandidate.latitude(),
                detailsCandidate.longitude() != null ? detailsCandidate.longitude() : baseCandidate.longitude(),
                detailsCandidate.rating() != null ? detailsCandidate.rating() : baseCandidate.rating(),
                detailsCandidate.addressComponents() != null && !detailsCandidate.addressComponents().isEmpty()
                        ? detailsCandidate.addressComponents()
                        : baseCandidate.addressComponents(),
                firstNonBlank(detailsCandidate.editorialSummary(), baseCandidate.editorialSummary()),
                detailsCandidate.regularOpeningWeekdayDescriptions() != null
                        && !detailsCandidate.regularOpeningWeekdayDescriptions().isEmpty()
                        ? detailsCandidate.regularOpeningWeekdayDescriptions()
                        : baseCandidate.regularOpeningWeekdayDescriptions(),
                firstNonBlank(detailsCandidate.firstPhotoName(), baseCandidate.firstPhotoName()),
                firstNonBlank(detailsCandidate.primaryType(), baseCandidate.primaryType()),
                detailsCandidate.types() != null && !detailsCandidate.types().isEmpty()
                        ? detailsCandidate.types()
                        : baseCandidate.types()
        );
    }

    private String resolveStoredScheduleLocationImageUrl(Place place) {
        String imageUrl = truncate(nullableTrim(place.getImageUrl()), IMAGE_URL_MAX_LENGTH);
        if (isLegacyPlaceholderImageUrl(imageUrl)) {
            return null;
        }
        return imageUrl;
    }

    private boolean isLegacyPlaceholderImageUrl(String imageUrl) {
        return hasText(imageUrl) && imageUrl.contains("cdn.lets-trip.com/place/");
    }

    private void appendScheduleLocationSearchToken(StringBuilder queryBuilder, String token) {
        if (!hasText(token)) {
            return;
        }
        if (!queryBuilder.isEmpty()) {
            queryBuilder.append(' ');
        }
        queryBuilder.append(token.trim());
    }

    private String resolveGoogleIncludedType(PlaceType placeType) {
        if (placeType == null) {
            return "tourist_attraction";
        }

        return switch (placeType) {
            case RESTAURANT -> "restaurant";
            case ACCOMMODATION -> "lodging";
            case SHOPPING -> "shopping_mall";
            case BEACH, NATURE, LANDMARK, CULTURE, ATTRACTION -> "tourist_attraction";
        };
    }

    private double calculateDistanceMeters(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2
    ) {
        double earthRadiusMeters = 6_371_000d;
        double latitudeDistance = Math.toRadians(latitude2 - latitude1);
        double longitudeDistance = Math.toRadians(longitude2 - longitude1);

        double a = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2)
                + Math.cos(Math.toRadians(latitude1))
                * Math.cos(Math.toRadians(latitude2))
                * Math.sin(longitudeDistance / 2)
                * Math.sin(longitudeDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusMeters * c;
    }

    private int calculateLevenshteinDistance(String left, String right) {
        if (left.equals(right)) {
            return 0;
        }

        int[][] distance = new int[left.length() + 1][right.length() + 1];

        for (int i = 0; i <= left.length(); i++) {
            distance[i][0] = i;
        }
        for (int j = 0; j <= right.length(); j++) {
            distance[0][j] = j;
        }

        for (int i = 1; i <= left.length(); i++) {
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                distance[i][j] = Math.min(
                        Math.min(distance[i - 1][j] + 1, distance[i][j - 1] + 1),
                        distance[i - 1][j - 1] + cost
                );
            }
        }

        return distance[left.length()][right.length()];
    }

    /**
     * 길찾기 버튼을 활성화할 수 있는지 판단합니다.
     * 좌표가 있으면 가장 안정적으로 길찾기가 가능하고, 좌표가 없어도 주소가 있으면 텍스트 기반 길찾기를 시도할 수 있습니다.
     *
     * @param tripSchedule 길찾기 가능 여부를 계산할 일정 엔티티
     * @param address 화면에서 사용할 기준 주소
     * @return 길찾기 가능 여부
     */
    private boolean canSearchRoute(TripSchedule tripSchedule, String address) {
        return hasLocation(tripSchedule) || hasText(address);
    }

    /**
     * 구글 길찾기 앱 연결 URL을 생성합니다.
     * 좌표가 모두 존재하면 좌표 기반 URL을 우선 사용하고, 좌표가 없으면 주소 기반 URL로 생성합니다.
     *
     * @param latitude 목적지 위도
     * @param longitude 목적지 경도
     * @param destinationAddress 목적지 주소
     * @return 구글 길찾기 연결 URL
     */
    private String buildGoogleDirectionsUrl(
            BigDecimal latitude,
            BigDecimal longitude,
            String destinationAddress
    ) {
        if (latitude != null && longitude != null) {
            return String.format(
                    GOOGLE_DIRECTIONS_COORDINATE_QUERY_TEMPLATE,
                    GOOGLE_DIRECTIONS_BASE_URL,
                    encodeQueryValue(latitude.toPlainString()),
                    encodeQueryValue(longitude.toPlainString())
            );
        }

        if (!hasText(destinationAddress)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        return String.format(
                GOOGLE_DIRECTIONS_ADDRESS_QUERY_TEMPLATE,
                GOOGLE_DIRECTIONS_BASE_URL,
                encodeQueryValue(destinationAddress.trim())
        );
    }

    /**
     * URL Query 파라미터에 안전하게 포함할 수 있도록 UTF-8 인코딩을 적용합니다.
     *
     * @param value Query 문자열 값
     * @return UTF-8 URL 인코딩 문자열
     */
    private String encodeQueryValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * 일정이 장소 좌표를 보유하고 있는지 확인합니다.
     *
     * @param tripSchedule 확인할 일정 엔티티
     * @return 위도와 경도가 모두 존재하면 true
     */
    private boolean hasLocation(TripSchedule tripSchedule) {
        return tripSchedule.getPlace() != null
                && tripSchedule.getPlace().getLatitude() != null
                && tripSchedule.getPlace().getLongitude() != null;
    }

    /**
     * 현재 정책상 본인 여행에 속한 일정은 상세 화면에서 편집 가능하므로 true를 반환합니다.
     * 향후 상태별 편집 제한 정책이 생기면 이 메서드만 수정해도 응답 값을 일괄 반영할 수 있습니다.
     *
     * @param tripSchedule 편집 가능 여부를 계산할 일정 엔티티
     * @return 일정 편집 가능 여부
     */
    private boolean canEditTripSchedule(TripSchedule tripSchedule) {
        return tripSchedule.getTrip() != null && Boolean.FALSE.equals(tripSchedule.getIsDeleted());
    }

    /**
     * 지도 보기 버튼은 지도에 표시할 좌표 기반 일정이 1건 이상 있을 때만 활성화합니다.
     *
     * @param locationScheduleCount 좌표 보유 일정 개수
     * @return 지도 보기 가능 여부
     */
    private boolean canViewMap(int locationScheduleCount) {
        return locationScheduleCount > 0;
    }

    /**
     * 현재 정책상 삭제되지 않은 내 여행은 상세 화면에서 편집할 수 있으므로 true를 반환합니다.
     *
     * @param trip 편집 가능 여부를 계산할 여행 엔티티
     * @return 여행 편집 가능 여부
     */
    private boolean canEditTrip(Trip trip) {
        return Boolean.FALSE.equals(trip.getIsDeleted());
    }

    /**
     * 현재 정책상 상세 화면에서 일정 추가를 막는 별도 상태 제한이 없으므로 true를 반환합니다.
     * 향후 완료 여행 잠금 정책이 생기면 이 메서드에서 일괄 반영할 수 있습니다.
     *
     * @param trip 일정 추가 가능 여부를 계산할 여행 엔티티
     * @return 일정 추가 가능 여부
     */
    private boolean canAddSchedule(Trip trip) {
        return Boolean.FALSE.equals(trip.getIsDeleted());
    }

    /**
     * 공백이 아닌 실제 문자열 값이 존재하는지 확인합니다.
     *
     * @param value 확인할 문자열
     * @return 값이 존재하면 true
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }

        return null;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String nullableTrim(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeMatchingText(String value) {
        String trimmed = nullableTrim(value);
        if (trimmed == null) {
            return "";
        }

        return trimmed
                .replaceAll("\\s+", "")
                .replace(",", "")
                .toLowerCase();
    }

    private record ScheduleLocationPlaceSnapshot(
            String placeName,
            String address,
            String description,
            String imageUrl
    ) {
    }

    private List<MyTripScheduleDateOptionResponseDto> buildDateOptions(Trip trip) {
        long tripDayCount = ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;

        return trip.getStartDate()
                .datesUntil(trip.getEndDate().plusDays(1))
                .map(scheduleDate -> MyTripScheduleDateOptionResponseDto.of(
                        calculateDayNo(trip.getStartDate(), scheduleDate),
                        scheduleDate
                ))
                .limit(tripDayCount)
                .toList();
    }

    /**
     * 여행 시작일 기준으로 특정 날짜가 몇 일차인지 계산합니다.
     *
     * @param tripStartDate 여행 시작일
     * @param scheduleDate 계산할 일정 날짜
     * @return 여행 기준 일차 번호
     */
    private int calculateDayNo(LocalDate tripStartDate, LocalDate scheduleDate) {
        return (int) ChronoUnit.DAYS.between(tripStartDate, scheduleDate) + 1;
    }

    /**
     * 계산된 여행 상태가 화면에서 선택한 필터 조건에 포함되는지 확인합니다.
     *
     * @param tripStatus 현재 날짜 기준으로 계산된 여행 상태
     * @param filterType 내 여행 화면에서 선택한 필터 유형
     * @return 필터 조건에 포함되면 true, 아니면 false
     */
    private boolean matchesTripFilter(TripStatus tripStatus, MyTripFilterType filterType) {
        return switch (filterType) {
            case ALL -> true;
            case UPCOMING -> tripStatus == TripStatus.PLANNED || tripStatus == TripStatus.ONGOING;
            case PAST -> tripStatus == TripStatus.COMPLETED;
        };
    }

    /**
     * 가까운 여행 일정 응답 DTO를 생성합니다.
     *
     * @param trip 가까운 여행으로 선택된 여행 엔티티
     * @param today 서비스 기준 현재 날짜
     * @param now 서비스 기준 현재 시간
     * @return 가까운 여행 일정 응답 DTO
     */
    private NearbyTripScheduleResponseDto buildNearbyTripScheduleResponse(Trip trip, LocalDate today, LocalTime now) {
        TripStatus tripStatus = resolveTripStatus(trip, today);
        List<TripSchedule> schedules = tripScheduleRepository
                .findAllWithPlaceByTrip_TripIdAndIsDeletedFalseOrderByScheduleDateAscStartTimeAsc(trip.getTripId());

        List<NearbyTripScheduleItemResponseDto> nextSchedules = filterNextSchedules(schedules, tripStatus, today, now)
                .stream()
                .limit(3)
                .map(NearbyTripScheduleItemResponseDto::from)
                .toList();

        return NearbyTripScheduleResponseDto.of(trip, tripStatus, today, schedules.size(), nextSchedules);
    }

    /**
     * 여행 시작일과 종료일을 기준으로 현재 여행 상태를 계산합니다.
     *
     * @param trip 여행 엔티티
     * @param today 서비스 기준 현재 날짜
     * @return 여행 예정, 여행 중, 여행 종료 중 하나의 여행 상태
     */
    private TripStatus resolveTripStatus(Trip trip, LocalDate today) {
        return resolveTripStatus(trip.getStartDate(), trip.getEndDate(), today);
    }

    /**
     * 여행 시작일과 종료일, 비교 날짜를 기준으로 여행 상태를 계산합니다.
     *
     * @param startDate 여행 시작일
     * @param endDate 여행 종료일
     * @param today 상태 계산 기준 날짜
     * @return 계산된 여행 상태
     */
    private TripStatus resolveTripStatus(LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (today.isBefore(startDate)) {
            return TripStatus.PLANNED;
        }
        if (today.isAfter(endDate)) {
            return TripStatus.COMPLETED;
        }
        return TripStatus.ONGOING;
    }

    /**
     * 내 여행 카드 응답을 화면 정렬 규칙에 맞춰 정렬하는 비교기를 생성합니다.
     * 여행 중, 여행 예정, 여행 종료 순으로 우선 정렬하고,
     * 상태가 같으면 예정/진행 중은 가까운 시작일, 종료는 최신 종료일 순으로 정렬합니다.
     *
     * @return 내 여행 카드 응답 정렬 비교기
     */
    private Comparator<MyTripSummaryResponseDto> buildMyTripSummaryComparator() {
        return (first, second) -> {
            int compareStatus = Integer.compare(
                    getTripStatusPriority(first.getTripStatus()),
                    getTripStatusPriority(second.getTripStatus())
            );
            if (compareStatus != 0) {
                return compareStatus;
            }

            if (first.getTripStatus() == TripStatus.COMPLETED) {
                int compareEndDate = second.getEndDate().compareTo(first.getEndDate());
                if (compareEndDate != 0) {
                    return compareEndDate;
                }
            } else {
                int compareStartDate = first.getStartDate().compareTo(second.getStartDate());
                if (compareStartDate != 0) {
                    return compareStartDate;
                }
            }

            return first.getTripId().compareTo(second.getTripId());
        };
    }

    /**
     * 여행 상태별로 화면 정렬 우선순위를 숫자로 변환합니다.
     *
     * @param tripStatus 현재 여행 상태
     * @return 정렬 우선순위 값
     */
    private int getTripStatusPriority(TripStatus tripStatus) {
        return switch (tripStatus) {
            case ONGOING -> 0;
            case PLANNED -> 1;
            case COMPLETED -> 2;
        };
    }

    /**
     * 홈 화면에 노출할 다음 일정 목록을 여행 상태 기준으로 필터링합니다.
     *
     * @param schedules 여행에 포함된 전체 일정 목록
     * @param tripStatus 현재 여행 상태
     * @param today 서비스 기준 현재 날짜
     * @param now 서비스 기준 현재 시간
     * @return 노출 가능한 다음 일정 목록
     */
    private List<TripSchedule> filterNextSchedules(
            List<TripSchedule> schedules,
            TripStatus tripStatus,
            LocalDate today,
            LocalTime now
    ) {
        if (tripStatus == TripStatus.COMPLETED) {
            return List.of();
        }

        if (tripStatus == TripStatus.PLANNED) {
            return schedules.stream()
                    .filter(schedule -> !schedule.getScheduleDate().isBefore(today))
                    .toList();
        }

        return schedules.stream()
                .filter(schedule -> schedule.getScheduleDate().isAfter(today)
                        || (schedule.getScheduleDate().isEqual(today)
                        && !schedule.getEndTime().isBefore(now)))
                .toList();
    }

    /**
     * 여행 엔티티에 공유 코드가 없으면 새 코드를 발급하고, 이미 있으면 기존 코드를 그대로 사용합니다.
     * 새 코드를 발급할 때는 active 여행과 충돌하지 않도록 간단한 중복 검사를 함께 수행합니다.
     *
     * @param trip 공유할 여행 엔티티
     * @return 중복되지 않는 여행 공유 코드
     */
    private String issueUniqueTripShareCode(Trip trip) {
        String currentShareCode = trip.getShareCode();
        if (currentShareCode != null && !currentShareCode.isBlank()) {
            return currentShareCode;
        }

        String issuedShareCode;
        do {
            issuedShareCode = UUID.randomUUID().toString().replace("-", "");
        } while (tripRepository.existsByShareCodeAndIsDeletedFalse(issuedShareCode));

        trip.updateShareCode(issuedShareCode);
        return issuedShareCode;
    }

    /**
     * 여행 제목을 공유 시트 상단 제목 문구로 변환합니다.
     *
     * @param trip 공유할 여행 엔티티
     * @return 공유 제목
     */
    private String createTripShareTitle(Trip trip) {
        return String.format(TRIP_SHARE_TITLE_TEMPLATE, trip.getTitle());
    }

    /**
     * 여행 기간과 제목을 조합해 공유 설명 문구를 생성합니다.
     *
     * @param trip 공유할 여행 엔티티
     * @return 공유 설명
     */
    private String createTripShareDescription(Trip trip) {
        return String.format(
                TRIP_SHARE_DESCRIPTION_TEMPLATE,
                trip.getStartDate().format(SHARE_DATE_FORMATTER),
                trip.getEndDate().format(SHARE_DATE_FORMATTER),
                trip.getTitle()
        );
    }

    /**
     * 발급된 공유 코드를 프런트 공유 링크 형식으로 변환합니다.
     *
     * @param shareCode 여행 공유 코드
     * @return 공유 링크
     */
    private String createTripShareUrl(String shareCode) {
        return String.format(TRIP_SHARE_URL_TEMPLATE, shareCode);
    }
}
