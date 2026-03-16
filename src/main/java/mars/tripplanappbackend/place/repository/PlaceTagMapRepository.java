package mars.tripplanappbackend.place.repository;

import mars.tripplanappbackend.domain.PlaceTagMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceTagMapRepository extends JpaRepository<PlaceTagMap, Long> {
}
