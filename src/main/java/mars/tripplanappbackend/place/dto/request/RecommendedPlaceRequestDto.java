package mars.tripplanappbackend.place.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메인 페이지 추천 여행지 조회 요청 DTO입니다.
 * 추천 개수는 쿼리 파라미터로 전달받고, 값이 없으면 기본값 5개를 사용합니다.
 */
@Getter
@NoArgsConstructor
@Schema(description = "추천 여행지 조회 요청 DTO")
public class RecommendedPlaceRequestDto {

    @Min(1)
    @Max(10)
    @Schema(description = "조회할 추천 여행지 개수", example = "5", defaultValue = "5")
    private Integer limit = 5;
}
