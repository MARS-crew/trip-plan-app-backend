package mars.tripplanappbackend.trip.repository;

import mars.tripplanappbackend.trip.domain.TripSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripScheduleRepository extends JpaRepository<TripSchedule, Long> {
    List<TripSchedule> findAllByTrip_TripIdAndIsDeletedFalseOrderByScheduleDateAscStartTimeAsc(Long tripId);

    List<TripSchedule> findAllByTrip_TripIdInAndIsDeletedFalse(List<Long> tripIds);

    List<TripSchedule> findAllByTrip_TripIdAndScheduleDateAndIsDeletedFalseOrderByStartTimeAsc(
            Long tripId,
            java.time.LocalDate scheduleDate
    );

    List<TripSchedule> findAllByScheduleDateAndStartTime(LocalDate scheduleDate, LocalTime startTime);

    Optional<TripSchedule> findTop1ByTripAndScheduleDateOrderByStartTime(Trip trip, LocalDate scheduleDate);

    Optional<TripSchedule> findByTripScheduleIdAndTrip_TripIdAndTrip_User_UsersIdAndIsDeletedFalse(
            Long tripScheduleId,
            Long tripId,
            String usersId
    );
}
