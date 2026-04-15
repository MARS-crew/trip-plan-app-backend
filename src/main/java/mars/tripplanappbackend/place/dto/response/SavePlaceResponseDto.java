package mars.tripplanappbackend.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.mypage.domain.SavedPlace;

/**
 * 장소 저장/저장 취소 결과를 공통 구조로 내려주는 응답 DTO입니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "장소 저장 응답 DTO")
public class SavePlaceResponseDto {

    @Schema(description = "저장 항목 PK", example = "12")
    private Long savedPlaceId;

    @Schema(description = "저장한 장소 PK", example = "7")
    private Long placeId;

    @Schema(description = "현재 저장 여부", example = "true")
    private boolean saved;

    /**
     * 장소 저장 처리 결과를 응답 DTO로 변환합니다.
     *
     * @param savedPlace 저장된 장소 엔티티
     * @return 저장 결과 응답 DTO
     */
    public static SavePlaceResponseDto saved(SavedPlace savedPlace) {
        return of(savedPlace, true);
    }

    /**
     * 장소 저장 취소 처리 결과를 응답 DTO로 변환합니다.
     *
     * @param savedPlace 저장 취소 처리된 장소 엔티티
     * @return 저장 취소 결과 응답 DTO
     */
    public static SavePlaceResponseDto unsaved(SavedPlace savedPlace) {
        return of(savedPlace, false);
    }

    /**
     * 저장 상태만 달라지는 저장/저장 취소 응답을 하나의 규격으로 생성합니다.
     *
     * @param savedPlace 저장 상태 변경 대상 엔티티
     * @param saved 현재 저장 여부
     * @return 저장 상태 응답 DTO
     */
    private static SavePlaceResponseDto of(SavedPlace savedPlace, boolean saved) {
        return SavePlaceResponseDto.builder()
                .savedPlaceId(savedPlace.getSavedPlaceId())
                .placeId(savedPlace.getPlace().getPlaceId())
                .saved(saved)
                .build();
    }
}
