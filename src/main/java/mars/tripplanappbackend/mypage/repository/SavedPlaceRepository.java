package mars.tripplanappbackend.mypage.repository;

import mars.tripplanappbackend.mypage.domain.SavedPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {

    boolean existsByUser_UserIdAndPlace_PlaceIdAndIsDeletedFalse(Long userId, Long placeId);
}
