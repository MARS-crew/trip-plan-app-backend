package mars.tripplanappbackend.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ReviewCreateResponseDto {
    @Schema(description = "리뷰 ID", example = "1")
    private Long reviewId;
}
