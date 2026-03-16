package mars.tripplanappbackend.trip.repository;

import mars.tripplanappbackend.domain.WishlistPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistPlaceRepository extends JpaRepository<WishlistPlace, Long> {
}
