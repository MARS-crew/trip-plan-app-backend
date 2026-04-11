package mars.tripplanappbackend.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.place.enums.SavedPlaceFilterType;

import java.util.List;

/**
 * 저장한 장소 목록 화면과 여행 추가 바텀시트 저장 탭에서 사용하는 응답 DTO입니다.
 * 저장 개수, 필터 상태, 빈 상태 여부, 카드 리스트를 한 번에 내려줍니다.
 */
@Getter
@Builder
@Schema(description = "저장한 장소 목록 응답 DTO")
public class SavedPlaceListResponseDto {

    @Schema(description = "현재 적용된 필터 유형", example = "ALL")
    private SavedPlaceFilterType filterType;

    @Schema(description = "전체 저장한 장소 개수", example = "5")
    private long savedPlaceCount;

    @Schema(description = "현재 필터 기준 저장한 장소 개수", example = "2")
    private long filteredSavedPlaceCount;

    @Schema(description = "현재 필터 결과가 비어 있는지 여부", example = "false")
    private boolean empty;

    @Schema(description = "저장한 장소 카드 목록")
    private List<SavedPlaceItemResponseDto> savedPlaces;

    /**
     * 저장한 장소 카드 목록을 저장한 장소 목록 응답 DTO로 변환합니다.
     *
     * @param filterType 현재 적용한 필터
     * @param savedPlaceCount 전체 저장한 장소 개수
     * @param savedPlaces 현재 필터 기준 저장한 장소 카드 목록
     * @return 저장한 장소 목록 응답 DTO
     */
    public static SavedPlaceListResponseDto of(
            SavedPlaceFilterType filterType,
            long savedPlaceCount,
            List<SavedPlaceItemResponseDto> savedPlaces
    ) {
        return SavedPlaceListResponseDto.builder()
                .filterType(filterType)
                .savedPlaceCount(savedPlaceCount)
                .filteredSavedPlaceCount(savedPlaces.size())
                .empty(savedPlaces.isEmpty())
                .savedPlaces(savedPlaces)
                .build();
    }
}
