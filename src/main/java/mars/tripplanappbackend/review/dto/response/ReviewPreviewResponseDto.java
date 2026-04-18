package mars.tripplanappbackend.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.trip.domain.VisitedPlace;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPreviewResponseDto {
    @Schema(description = "방문 장소 ID", example = "1")
    private Long visitedPlaceId;

    @Schema(description = "장소 이름", example = "센소지 아사쿠사")
    private String placeName;

    @Schema(description = "방문 날짜", example = "2026-02-28")
    private LocalDate visitedAt;

    public ReviewPreviewResponseDto(VisitedPlace visitedPlace) {
            this(
                    visitedPlace.getVisitedPlaceId(),
                    visitedPlace.getPlace().getName(),
                    visitedPlace.getVisitedAt().toLocalDate()
            );
        }
}
