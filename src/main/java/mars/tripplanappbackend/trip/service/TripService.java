package mars.tripplanappbackend.trip.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import mars.tripplanappbackend.trip.domain.Trip;
import mars.tripplanappbackend.trip.domain.TripSchedule;
import mars.tripplanappbackend.trip.dto.request.NearbyTripScheduleRequestDto;
import mars.tripplanappbackend.trip.dto.response.NearbyTripScheduleItemResponseDto;
import mars.tripplanappbackend.trip.dto.response.NearbyTripScheduleResponseDto;
import mars.tripplanappbackend.trip.enums.TripStatus;
import mars.tripplanappbackend.trip.repository.TripRepository;
import mars.tripplanappbackend.trip.repository.TripScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 메인 페이지에 필요한 여행 일정 조회 로직을 처리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {

    private final MyPageRepository myPageRepository;
    private final TripRepository tripRepository;
    private final TripScheduleRepository tripScheduleRepository;

    /**
     * 홈 화면 상단 카드에 사용할 가까운 여행일정 정보를 조회한다.
     * 진행 중인 여행이 있으면 해당 여행을 우선 조회하고,
     * 없으면 가장 가까운 예정 여행을 조회한다.
     *
     * @param requestDto 조회 대상 사용자 PK를 담은 요청 DTO
     * @return 가까운 여행일정 응답 DTO
     */
    public NearbyTripScheduleResponseDto getNearbyTripSchedule(NearbyTripScheduleRequestDto requestDto) {
        validateUserExists(requestDto);

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
     * 요청한 사용자 PK가 실제 회원으로 존재하는지 검증한다.
     *
     * @param requestDto 사용자 PK를 담은 요청 DTO
     */
    private void validateUserExists(NearbyTripScheduleRequestDto requestDto) {
        if (!myPageRepository.existsById(requestDto.getUserId())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }

    /**
     * 선택된 여행 엔티티를 메인 카드 응답 DTO로 변환한다.
     *
     * @param trip 가까운 여행으로 선택된 여행 엔티티
     * @param today 서버 기준 현재 날짜
     * @param now 서버 기준 현재 시간
     * @return 가까운 여행일정 응답 DTO
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
     * 여행 시작일과 종료일을 기준으로 현재 여행 상태를 계산한다.
     *
     * @param trip 여행 엔티티
     * @param today 서버 기준 현재 날짜
     * @return 예정, 진행 중, 종료 중 하나의 여행 상태
     */
    private TripStatus resolveTripStatus(Trip trip, LocalDate today) {
        if (today.isBefore(trip.getStartDate())) {
            return TripStatus.PLANNED;
        }
        if (today.isAfter(trip.getEndDate())) {
            return TripStatus.COMPLETED;
        }
        return TripStatus.ONGOING;
    }

    /**
     * 메인 화면에 노출할 다음 일정 목록을 여행 상태별 기준으로 걸러낸다.
     *
     * @param schedules 여행에 속한 전체 일정 목록
     * @param tripStatus 현재 여행 상태
     * @param today 서버 기준 현재 날짜
     * @param now 서버 기준 현재 시간
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
}
