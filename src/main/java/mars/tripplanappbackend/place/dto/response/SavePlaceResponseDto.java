package mars.tripplanappbackend.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.mypage.domain.SavedPlace;

/**
 * 여행지 저장 항목 추가 응답 DTO입니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "여행지 저장 항목 추가 응답 DTO")
public class SavePlaceResponseDto {

    @Schema(description = "저장 항목 PK", example = "12")
    private Long savedPlaceId;

    @Schema(description = "저장된 장소 PK", example = "7")
    private Long placeId;

    @Schema(description = "저장 완료 여부", example = "true")
    private boolean saved;

    /**
     * 저장된 장소 엔티티를 저장 응답 DTO로 변환합니다.
     *
     * @param savedPlace 저장된 장소 엔티티
     * @return 저장 응답 DTO
     */
    public static SavePlaceResponseDto from(SavedPlace savedPlace) {
        return SavePlaceResponseDto.builder()
                .savedPlaceId(savedPlace.getSavedPlaceId())
                .placeId(savedPlace.getPlace().getPlaceId())
                .saved(true)
                .build();
    }
}
