package mars.tripplanappbackend.trip.repository;

import mars.tripplanappbackend.trip.domain.VisitedPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VisitedPlaceRepository extends JpaRepository<VisitedPlace, Long> {
    long countByUser_UserIdAndIsDeletedFalse(Long userId);

    java.util.List<VisitedPlace> findAllByTrip_TripIdAndIsDeletedFalse(Long tripId);

    boolean existsByTrip_TripIdAndPlace_PlaceIdAndIsDeletedFalse(Long tripId, Long placeId);

    Optional<VisitedPlace> findByVisitedPlaceIdAndIsDeletedFalse(Long visitedPlaceId);

    List<VisitedPlace> findByUser_UsersIdAndIsDeletedFalseOrderByVisitedAtDesc(String usersId);
}
