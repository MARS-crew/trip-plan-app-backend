package mars.tripplanappbackend.trip.repository;

import mars.tripplanappbackend.trip.domain.WishlistPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistPlaceRepository extends JpaRepository<WishlistPlace, Long> {
    java.util.List<WishlistPlace> findAllByTrip_TripIdAndIsDeletedFalse(Long tripId);

    boolean existsByTrip_TripIdAndPlace_PlaceIdAndIsDeletedFalse(Long tripId, Long placeId);

    java.util.List<WishlistPlace> findAllByTrip_TripIdAndIsDeletedFalseAndPlace_IsDeletedFalseOrderByCreatedAtDesc(Long tripId);

    java.util.Optional<WishlistPlace> findByWishlistPlaceIdAndTrip_TripIdAndTrip_User_UsersIdAndIsDeletedFalse(
            Long wishlistPlaceId,
            Long tripId,
            String usersId
    );
}
