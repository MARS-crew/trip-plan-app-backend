package mars.tripplanappbackend.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.review.domain.Review;
import mars.tripplanappbackend.review.domain.ReviewImage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateResponseDto {
    @Schema(description = "생성된 리뷰 ID", example = "1")
    private Long reviewId;

    @Schema(description = "별점 (1-5)", example = "4")
    private Integer rating;

    @Schema(description = "리뷰 내용", example = "너무 멋있습니다.")
    private String content;

    @Schema(description = "방문 일자", example = "2026-02-28")
    private LocalDate visitedDate;

    @Schema(description = "저장된 이미지 경로 리스트", example = "reviews/1/uuid_123.png")
    private List<String> imageUrls;

    @Schema(description = "리뷰 작성 일시", example = "2026-04-16T20:30:00")
    private LocalDateTime createdAt;

    public ReviewCreateResponseDto(Review review, List<ReviewImage> images) {
        this.reviewId = review.getReviewId();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.visitedDate = review.getVisitedPlace().getVisitedAt().toLocalDate();
        this.imageUrls = images.stream()
                .map(ReviewImage::getImageUrl)
                .toList();
        this.createdAt = review.getCreatedAt();
    }
}
