package mars.tripplanappbackend.trip.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.repository.PlaceRepository;
import mars.tripplanappbackend.trip.dto.request.AddWishlistPlaceRequestDto;
import mars.tripplanappbackend.trip.domain.Trip;
import mars.tripplanappbackend.trip.domain.TripSchedule;
import mars.tripplanappbackend.trip.domain.VisitedPlace;
import mars.tripplanappbackend.trip.domain.WishlistPlace;
import mars.tripplanappbackend.trip.dto.request.CreateTripRequestDto;
import mars.tripplanappbackend.trip.dto.request.DeleteTripRequestDto;
import mars.tripplanappbackend.trip.dto.request.DeleteTripScheduleRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripFilterRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripListRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripScheduleByDateRequestDto;
import mars.tripplanappbackend.trip.dto.request.MyTripScheduleListRequestDto;
import mars.tripplanappbackend.trip.dto.request.NearbyTripScheduleRequestDto;
import mars.tripplanappbackend.trip.dto.request.ShareTripRequestDto;
import mars.tripplanappbackend.trip.dto.request.UpdateTripRequestDto;
import mars.tripplanappbackend.trip.dto.response.AddWishlistPlaceResponseDto;
import mars.tripplanappbackend.trip.dto.response.CreateTripResponseDto;
import mars.tripplanappbackend.trip.dto.response.DeleteTripResponseDto;
import mars.tripplanappbackend.trip.dto.response.DeleteTripScheduleResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripDailyScheduleResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripListResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripScheduleByDateResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripScheduleDateOptionResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripScheduleItemResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripScheduleListResponseDto;
import mars.tripplanappbackend.trip.dto.response.MyTripSummaryResponseDto;
import mars.tripplanappbackend.trip.dto.response.NearbyTripScheduleItemResponseDto;
import mars.tripplanappbackend.trip.dto.response.NearbyTripScheduleResponseDto;
import mars.tripplanappbackend.trip.dto.response.ShareTripResponseDto;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private static final String TRIP_SHARE_URL_TEMPLATE = "https://lets-trip.com/trips/share/%s";
    private static final String TRIP_SHARE_TITLE_TEMPLATE = "Let's Trip에서 %s 일정을 확인해보세요.";
    private static final String TRIP_SHARE_DESCRIPTION_TEMPLATE = "%s부터 %s까지의 %s 일정을 공유합니다.";
    private static final DateTimeFormatter SHARE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final MyPageRepository myPageRepository;
    private final PlaceRepository placeRepository;
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

        TripStatus tripStatus = resolveTripStatus(requestDto.getStartDate(), requestDto.getEndDate(), LocalDate.now());

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
                .tripStatus(resolveTripStatus(requestDto.getStartDate(), requestDto.getEndDate(), LocalDate.now()))
                .user(user)
                .build();

        Trip savedTrip = tripRepository.save(trip);
        TripStatus tripStatus = resolveTripStatus(savedTrip, LocalDate.now());

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
        List<MyTripScheduleItemResponseDto> schedules = tripScheduleRepository
                .findAllByTrip_TripIdAndScheduleDateAndIsDeletedFalseOrderByStartTimeAsc(trip.getTripId(), selectedDate)
                .stream()
                .map(MyTripScheduleItemResponseDto::from)
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
     * 내 여행 상세 화면 본문 전체를 구성하기 위해 여행 기간 전체를 기준으로 일차별 일정 리스트를 조회합니다.
     * 특정 날짜만 내려주는 날짜별 조회와 달리 시작일부터 종료일까지 모든 날짜를 순회하며,
     * 일정이 없는 날짜도 빈 리스트로 포함해 화면에서 일차 섹션을 그대로 렌더링할 수 있도록 합니다.
     *
     * @param requestDto 조회 대상 여행 PK와 로그인 사용자 아이디를 담은 요청 DTO
     * @return 여행 기본 정보와 일차별 일정 섹션 목록을 담은 응답 DTO
     */
    public MyTripScheduleListResponseDto getMyTripScheduleList(MyTripScheduleListRequestDto requestDto) {
        validateTripScheduleListRequest(requestDto);
        validateUserExistsByUsersId(requestDto.getUsersId());

        Trip trip = tripRepository.findByTripIdAndUser_UsersIdAndIsDeletedFalse(
                        requestDto.getTripId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        List<TripSchedule> tripSchedules = tripScheduleRepository
                .findAllByTrip_TripIdAndIsDeletedFalseOrderByScheduleDateAscStartTimeAsc(trip.getTripId());

        Map<LocalDate, List<MyTripScheduleItemResponseDto>> schedulesByDate = tripSchedules.stream()
                .collect(Collectors.groupingBy(
                        TripSchedule::getScheduleDate,
                        LinkedHashMap::new,
                        Collectors.mapping(MyTripScheduleItemResponseDto::from, Collectors.toList())
                ));

        long tripDayCount = ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
        List<MyTripDailyScheduleResponseDto> dailySchedules =
                buildDailyScheduleSections(trip, schedulesByDate, tripDayCount);

        return MyTripScheduleListResponseDto.of(
                trip.getTripId(),
                trip.getTitle(),
                trip.getStartDate(),
                trip.getEndDate(),
                tripDayCount,
                tripSchedules.size(),
                dailySchedules
        );
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

        if (requestDto.getScheduleDate().isBefore(trip.getStartDate())
                || requestDto.getScheduleDate().isAfter(trip.getEndDate())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

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

        int dayNo = calculateDayNo(trip.getStartDate(), requestDto.getScheduleDate());

        return AddWishlistPlaceResponseDto.from(wishlistPlace, requestDto.getScheduleDate(), dayNo);
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
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

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
        if (!myPageRepository.existsByUsersId(usersId)) {
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
     * 일정 리스트 조회 요청에 필요한 여행 PK와 로그인 사용자 아이디가 모두 존재하는지 검증합니다.
     *
     * @param requestDto 일정 리스트 조회 요청 DTO
     */
    private void validateTripScheduleListRequest(MyTripScheduleListRequestDto requestDto) {
        if (requestDto.getTripId() == null || requestDto.getTripId() < 1 || requestDto.getUsersId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 위시리스트 장소 추가 요청에 필요한 여행 PK, 장소 PK, 선택 날짜, 사용자 아이디 존재 여부를 검증합니다.
     *
     * @param requestDto 위시리스트 장소 추가 요청 DTO
     */
    private void validateAddWishlistPlaceRequest(AddWishlistPlaceRequestDto requestDto) {
        if (requestDto.getTripId() == null
                || requestDto.getTripId() < 1
                || requestDto.getPlaceId() == null
                || requestDto.getPlaceId() < 1
                || requestDto.getScheduleDate() == null
                || requestDto.getUsersId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 사용자 PK가 실제 사용자 테이블에 존재하는지 검증합니다.
     *
     * @param userId 로그인 사용자 PK
     */
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
        return myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 여행 추가 요청의 시작일과 종료일이 화면 기획 조건에 맞는지 검증합니다.
     * 시작일이 오늘보다 이전일 수 없고, 종료일은 시작일보다 빠를 수 없습니다.
     *
     * @param startDate 여행 시작일
     * @param endDate 여행 종료일
     */
    private void validateCreateTripDates(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();

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
        LocalDate today = LocalDate.now();

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

    /**
     * 선택 가능한 날짜 드롭다운 목록을 여행 시작일부터 종료일까지 생성합니다.
     *
     * @param trip 조회 대상 여행 엔티티
     * @return 날짜 드롭다운 옵션 목록
     */
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
                .findAllByTrip_TripIdAndIsDeletedFalseOrderByScheduleDateAscStartTimeAsc(trip.getTripId());

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
