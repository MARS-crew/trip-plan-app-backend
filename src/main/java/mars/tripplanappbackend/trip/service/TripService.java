package mars.tripplanappbackend.trip.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.trip.domain.Trip;
import mars.tripplanappbackend.trip.enums.TripStatus;
import mars.tripplanappbackend.trip.repository.TripRepository;
import mars.tripplanappbackend.trip.repository.TripScheduleRepository;
import mars.tripplanappbackend.trip.repository.VisitedPlaceRepository;
import mars.tripplanappbackend.trip.repository.WishlistPlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {

    private final TripRepository tripRepository;
    private final TripScheduleRepository tripScheduleRepository;
    private final VisitedPlaceRepository visitedPlaceRepository;
    private final WishlistPlaceRepository wishlistPlaceRepository;

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
}
