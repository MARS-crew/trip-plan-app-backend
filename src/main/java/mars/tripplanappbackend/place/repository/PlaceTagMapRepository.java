package mars.tripplanappbackend.place.repository;

import mars.tripplanappbackend.place.domain.PlaceTagMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceTagMapRepository extends JpaRepository<PlaceTagMap, Long> {
    List<PlaceTagMap> findAllByPlace_PlaceIdAndIsDeletedFalse(Long placeId);
}
