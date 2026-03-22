package mars.tripplanappbackend.place.repository;

import mars.tripplanappbackend.place.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    Optional<Place> findByPlaceIdAndIsDeletedFalse(Long placeId);
}
