package mars.tripplanappbackend.mypage.repository;

import mars.tripplanappbackend.mypage.domain.SavedPlace;
import mars.tripplanappbackend.mypage.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {
    boolean existsByUser_UsersIdAndPlace_PlaceIdAndIsDeletedFalse(String usersId, Long placeId);

    long countByUserAndIsDeletedFalse(User user);

    long countByUser_UsersIdAndIsDeletedFalseAndPlace_IsDeletedFalse(String usersId);

    List<SavedPlace> findAllByUser_UsersIdAndIsDeletedFalseAndPlace_IsDeletedFalseOrderByCreatedAtDesc(String usersId);

    List<SavedPlace> findAllByUser_UsersIdAndIsDeletedFalseAndPlace_IsDeletedFalseAndPlace_PlaceTypeOrderByCreatedAtDesc(
            String usersId,
            mars.tripplanappbackend.place.enums.PlaceType placeType
    );
}
