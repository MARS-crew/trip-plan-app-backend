package mars.tripplanappbackend.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewListResponseDto {
    @Schema(description = "작성자 닉네임", example = "여행가자")
    private String nickname;

    @Schema(description = "부여한 별점 (1~5)", example = "5")
    private Integer rating;

    @Schema(description = "리뷰 본문", example = "경치가 정말 환상적이었어요!")
    private String content;

    @Schema(description = "리뷰 이미지 URL 리스트", example = "[\"reviews/1/a.png\", \"reviews/1/b.png\"]")
    private List<String> imageUrls;

    @Schema(description = "실제 장소 방문 날짜", example = "2026-04-12")
    private LocalDate visitedDate;

    @Schema(description = "리뷰 작성 일시", example = "2026-04-15T14:30:00")
    private LocalDateTime createdAt;
}
