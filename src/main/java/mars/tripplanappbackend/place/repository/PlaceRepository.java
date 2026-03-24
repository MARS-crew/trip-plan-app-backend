package mars.tripplanappbackend.place.repository;

import mars.tripplanappbackend.place.domain.Place;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByIsDeletedFalseOrderByRatingAvgDescReviewCountDesc(Pageable pageable);
}
