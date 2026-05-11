package mars.tripplanappbackend.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequestDto {
    @Schema(description = "방문한 장소 ID", example = "1")
    @NotNull(message = "방문한 장소 ID를 입력해 주세요.")
    private Long visitedPlaceId;

    @Schema(description = "장소 이름", example = "삿포로 시계탑")
    @NotBlank(message = "방문 장소를 적어 주세요.")
    private String placeName;

    @Schema(description = "방문 날짜", example = "2026-04-12")
    @NotNull(message = "방문한 날짜를 적어 주세요.")
    private LocalDate visitedAt;

    @Schema(description = "별점 (1-5)", example = "4")
    @Min(value = 1, message = "별점은 최소 1점 이상이어야 합니다.")
    @Max(value = 5, message = "별점은 최대 5점까지 가능합니다.")
    @NotNull(message = "별점은 최소 1점부터 등록됩니다.")
    private Integer rating;

    @Schema(description = "리뷰 본문", example = "정말 멋잇어요...")
    @Size(max = 500, message = "리뷰는 최대 500자까지 작성 가능합니다.")
    @NotBlank(message = "본문을 적어 주세요.")
    private String content;

    @Schema(description = "리뷰 이미지 URL 리스트", example =  "[\"reviews/1/a.png\", \"reviews/1/b.png\"]")
    @Size(max = 3, message = "이미지는 최대 3장까지 가능합니다.")
    private List<String> imageUrls;
}
