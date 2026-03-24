package mars.tripplanappbackend.trip.repository;

import mars.tripplanappbackend.trip.domain.TripSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TripScheduleRepository extends JpaRepository<TripSchedule, Long> {
    List<TripSchedule> findAllByTrip_TripIdAndIsDeletedFalseOrderByScheduleDateAscStartTimeAsc(Long tripId);
}
