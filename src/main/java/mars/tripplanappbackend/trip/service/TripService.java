package mars.tripplanappbackend.trip.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import mars.tripplanappbackend.trip.domain.Trip;
import mars.tripplanappbackend.trip.domain.TripSchedule;
import mars.tripplanappbackend.trip.dto.response.NearbyTripScheduleItemResponseDto;
import mars.tripplanappbackend.trip.dto.response.NearbyTripScheduleResponseDto;
import mars.tripplanappbackend.trip.enums.TripStatus;
import mars.tripplanappbackend.trip.repository.TripRepository;
import mars.tripplanappbackend.trip.repository.TripScheduleRepository;
import mars.tripplanappbackend.trip.repository.VisitedPlaceRepository;
import mars.tripplanappbackend.trip.repository.WishlistPlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {

    private final MyPageRepository myPageRepository;
    private final TripRepository tripRepository;
    private final TripScheduleRepository tripScheduleRepository;
    private final VisitedPlaceRepository visitedPlaceRepository;
    private final WishlistPlaceRepository wishlistPlaceRepository;

    public NearbyTripScheduleResponseDto getNearbyTripSchedule(Long userId) {
        validateUserExists(userId);

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

    public List<Trip> getTripsByStatus(Long userId, TripStatus status) {
        return tripRepository.findAllByUser_UserIdAndIsDeletedFalse(userId)
                .stream()
                .filter(trip -> status == null || resolveTripStatus(trip) == status)
                .toList();
    }

    public TripStatus resolveTripStatus(Trip trip) {
        return resolveTripStatus(trip.getStartDate(), trip.getEndDate(), LocalDate.now());
    }

    TripStatus resolveTripStatus(LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (today.isBefore(startDate)) {
            return TripStatus.PLANNED;
        }
        if (today.isAfter(endDate)) {
            return TripStatus.COMPLETED;
        }
        return TripStatus.ONGOING;
    }

    private void validateUserExists(Long userId) {
        if (!myPageRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private NearbyTripScheduleResponseDto buildNearbyTripScheduleResponse(Trip trip, LocalDate today, LocalTime now) {
        TripStatus tripStatus = resolveTripStatus(trip);
        List<TripSchedule> schedules = tripScheduleRepository
                .findAllByTrip_TripIdAndIsDeletedFalseOrderByScheduleDateAscStartTimeAsc(trip.getTripId());

        List<TripSchedule> nextSchedules = filterNextSchedules(schedules, tripStatus, today, now);

        return NearbyTripScheduleResponseDto.builder()
                .hasNearbyTrip(true)
                .tripId(trip.getTripId())
                .tripTitle(trip.getTitle())
                .tripStatus(tripStatus)
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .tripDayCount(ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1)
                .daysUntilTrip(tripStatus == TripStatus.PLANNED ? ChronoUnit.DAYS.between(today, trip.getStartDate()) : null)
                .scheduleCount(schedules.size())
                .nextSchedules(nextSchedules.stream()
                        .limit(3)
                        .map(this::toNearbyTripScheduleItemResponse)
                        .toList())
                .build();
    }

    private List<TripSchedule> filterNextSchedules(List<TripSchedule> schedules, TripStatus tripStatus, LocalDate today, LocalTime now) {
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

    private NearbyTripScheduleItemResponseDto toNearbyTripScheduleItemResponse(TripSchedule tripSchedule) {
        return NearbyTripScheduleItemResponseDto.builder()
                .tripScheduleId(tripSchedule.getTripScheduleId())
                .scheduleDate(tripSchedule.getScheduleDate())
                .startTime(tripSchedule.getStartTime())
                .endTime(tripSchedule.getEndTime())
                .title(tripSchedule.getTitle())
                .placeName(tripSchedule.getPlace() != null ? tripSchedule.getPlace().getName() : null)
                .address(tripSchedule.getAddress())
                .memo(tripSchedule.getMemo())
                .build();
    }
}
