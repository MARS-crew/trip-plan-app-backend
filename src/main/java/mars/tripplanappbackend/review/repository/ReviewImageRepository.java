package mars.tripplanappbackend.review.repository;

import mars.tripplanappbackend.review.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findAllByReview_ReviewIdInAndIsDeletedFalseOrderBySortOrderAsc(List<Long> reviewIds);
}
