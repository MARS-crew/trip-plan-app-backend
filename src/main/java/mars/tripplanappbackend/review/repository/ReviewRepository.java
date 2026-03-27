package mars.tripplanappbackend.review.repository;

import mars.tripplanappbackend.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findTop3ByPlace_PlaceIdAndIsDeletedFalseOrderByCreatedAtDesc(Long placeId);
}
