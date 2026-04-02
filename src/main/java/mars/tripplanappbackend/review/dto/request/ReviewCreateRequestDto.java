package mars.tripplanappbackend.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.review.domain.Review;
import mars.tripplanappbackend.trip.domain.VisitedPlace;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequestDto {

    @Schema(description = "장소 ID", example = "1")
    @NotNull(message = "필수 입력값입니다.")
    private Long placeId;

    @Schema(description = "방문 기록 ID", example = "10")
    @NotNull(message = "필수 입력값입니다.")
    private Long visitedPlaceId;

    @Schema(description = "별점 (1~5)", example = "5")
    @NotNull(message = "필수 입력값입니다.")
    @Min(value = 1, message = "최소 1점 이상이어야 합니다.")
    @Max(value = 5, message = "최대 5점까지 가능합니다.")
    private Integer rating;

    @Schema(description = "리뷰 내용", example = "진짜 맛있었어요!")
    @NotBlank(message = "필수 입력값입니다.")
    @Size(max = 500, message = "500자 이하로 입력해주세요.")
    private String content;
    
}
