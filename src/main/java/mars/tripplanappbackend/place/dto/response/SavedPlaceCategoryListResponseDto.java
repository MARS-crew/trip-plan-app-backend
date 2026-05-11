package mars.tripplanappbackend.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 저장한 장소 카테고리 목록 응답 DTO입니다.
 * 저장된 장소 조회 API와 별도로, 카테고리 탭 구성에 필요한 메타 정보를 제공합니다.
 */
@Getter
@Builder
@Schema(description = "저장한 장소 카테고리 목록 응답 DTO")
public class SavedPlaceCategoryListResponseDto {

    private static final String EMPTY_MESSAGE = "저장된 장소가 없습니다.";

    @Schema(description = "저장된 장소 전체 개수", example = "12")
    private long totalSavedPlaceCount;

    @Schema(description = "카테고리 개수", example = "9")
    private int categoryCount;

    @Schema(description = "저장된 장소가 비어 있는지 여부", example = "false")
    private boolean empty;

    @Schema(description = "빈 상태에서 표시할 안내 문구", example = "저장된 장소가 없습니다.", nullable = true)
    private String emptyMessage;

    @Schema(description = "카테고리 목록")
    private List<SavedPlaceCategoryResponseDto> categories;

    /**
     * 저장한 장소 카테고리 목록 응답 DTO를 생성합니다.
     *
     * @param totalSavedPlaceCount 저장된 장소 전체 개수
     * @param categories 카테고리별 저장 개수 목록
     * @return 저장한 장소 카테고리 목록 응답 DTO
     */
    public static SavedPlaceCategoryListResponseDto of(
            long totalSavedPlaceCount,
            List<SavedPlaceCategoryResponseDto> categories
    ) {
        boolean empty = totalSavedPlaceCount == 0;

        return SavedPlaceCategoryListResponseDto.builder()
                .totalSavedPlaceCount(totalSavedPlaceCount)
                .categoryCount(categories.size())
                .empty(empty)
                .emptyMessage(empty ? EMPTY_MESSAGE : null)
                .categories(categories)
                .build();
    }
}

