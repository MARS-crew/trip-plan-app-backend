package mars.tripplanappbackend.review.repository;

import mars.tripplanappbackend.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findTop3ByPlace_PlaceIdAndIsDeletedFalseOrderByCreatedAtDesc(Long placeId);

    boolean existsByVisitedPlace_VisitedPlaceIdAndIsDeletedFalse(Long visitedPlaceId);

    List<Review> findByPlace_PlaceIdAndIsDeletedFalseOrderByCreatedAtDesc(Long placeId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r " +
            "WHERE r.place.placeId = :placeId AND r.isDeleted = false " +
            "GROUP BY r.rating")
    List<Object[]> countReviewsByRating(@Param("placeId") Long placeId);
}
