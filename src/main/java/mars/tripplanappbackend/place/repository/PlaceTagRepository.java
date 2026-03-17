package mars.tripplanappbackend.place.repository;

import mars.tripplanappbackend.place.domain.PlaceTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceTagRepository extends JpaRepository<PlaceTag, Long> {
}
