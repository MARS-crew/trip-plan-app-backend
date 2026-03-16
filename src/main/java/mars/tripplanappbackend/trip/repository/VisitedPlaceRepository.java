package mars.tripplanappbackend.trip.repository;

import mars.tripplanappbackend.domain.VisitedPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitedPlaceRepository extends JpaRepository<VisitedPlace, Long> {
}
