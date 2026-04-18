package mars.tripplanappbackend.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewInfoResponseDto {
    @Schema(description = "평균 별점", example = "4.5")
    private BigDecimal ratingAvg;

    @Schema(description = "전체 리뷰 개수", example = "128")
    private Integer reviewCount;

    @Schema(description = "별점별 분포", example = "{5: 80, 4: 30, 3: 10, 2: 5, 1: 3}")
    private Map<Integer, Long> ratingDistribution;

    @Schema(description = "리뷰 목록")
    private List<ReviewListResponseDto> reviews;
}
