package mars.tripplanappbackend.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.review.domain.Review;

import java.time.LocalDate;
import java.util.List;

/**
 * 여행지 상세 페이지의 리뷰 미리보기 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "여행지 상세 리뷰 미리보기")
public class PlaceReviewPreviewResponseDto {

    @Schema(description = "리뷰 PK", example = "31")
    private Long reviewId;

    @Schema(description = "작성자 닉네임", example = "민영")
    private String nickname;

    @Schema(description = "별점", example = "5")
    private Integer rating;

    @Schema(description = "리뷰 내용", example = "야경도 좋았고 산책하기 좋았어요.")
    private String content;

    @Schema(description = "방문일", example = "2026-03-18")
    private LocalDate visitedDate;

    @Schema(description = "리뷰 이미지 URL 목록", example = "[\"https://cdn.lets-trip.com/review/31-1.jpg\"]")
    private List<String> imageUrls;

    /**
     * 리뷰 엔티티와 이미지 목록을 리뷰 미리보기 DTO로 변환합니다.
     *
     * @param review 리뷰 엔티티
     * @param imageUrls 리뷰 이미지 URL 목록
     * @return 리뷰 미리보기 응답 DTO
     */
    public static PlaceReviewPreviewResponseDto from(Review review, List<String> imageUrls) {
        return PlaceReviewPreviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .nickname(review.getUser().getNickname())
                .rating(review.getRating())
                .content(review.getContent())
                .visitedDate(review.getVisitedDate())
                .imageUrls(imageUrls)
                .build();
    }
}
