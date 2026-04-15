package mars.tripplanappbackend.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.place.enums.SavedPlaceFilterType;

/**
 * 저장한 장소 카테고리 단건 응답 DTO입니다.
 * 카테고리 코드/라벨/저장 개수를 함께 내려 필터 탭 렌더링에 사용합니다.
 */
@Getter
@Builder
@Schema(description = "저장한 장소 카테고리 단건 응답")
public class SavedPlaceCategoryResponseDto {

    @Schema(description = "카테고리 필터 코드", example = "ATTRACTION")
    private SavedPlaceFilterType filterType;

    @Schema(description = "카테고리 라벨", example = "관광지")
    private String filterDisplayName;

    @Schema(description = "해당 카테고리 저장 개수", example = "3")
    private long savedPlaceCount;

    @Schema(description = "해당 카테고리에 저장된 장소 존재 여부", example = "true")
    private boolean hasSavedPlaces;

    /**
     * 저장한 장소 카테고리 정보를 단건 응답 DTO로 변환합니다.
     *
     * @param filterType 카테고리 필터 코드
     * @param savedPlaceCount 해당 카테고리 저장 개수
     * @return 저장한 장소 카테고리 단건 응답 DTO
     */
    public static SavedPlaceCategoryResponseDto of(SavedPlaceFilterType filterType, long savedPlaceCount) {
        return SavedPlaceCategoryResponseDto.builder()
                .filterType(filterType)
                .filterDisplayName(filterType.getDisplayName())
                .savedPlaceCount(savedPlaceCount)
                .hasSavedPlaces(savedPlaceCount > 0)
                .build();
    }
}

