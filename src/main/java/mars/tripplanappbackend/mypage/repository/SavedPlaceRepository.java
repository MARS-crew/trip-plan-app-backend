package mars.tripplanappbackend.mypage.repository;

import mars.tripplanappbackend.mypage.domain.SavedPlace;
import mars.tripplanappbackend.mypage.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {
    boolean existsByUser_UsersIdAndPlace_PlaceIdAndIsDeletedFalse(String usersId, Long placeId);

    long countByUserAndIsDeletedFalse(User user);
}
