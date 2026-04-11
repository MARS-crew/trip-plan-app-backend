package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 내 여행 상세의 장소 선택 화면에서 사용할 저장한 장소/위시리스트 목록 응답 DTO입니다.
 */
@Schema(description = "저장한 장소/위시리스트 조회 응답 DTO")
public class TripPlaceSelectionResponseDto {

    @Schema(description = "조회한 여행 PK", example = "5")
    private final Long tripId;

    @Schema(description = "조회한 여행 제목", example = "오사카 여행")
    private final String tripTitle;

    @Schema(description = "저장한 장소 개수", example = "3")
    private final int savedPlaceCount;

    @Schema(description = "위시리스트 장소 개수", example = "2")
    private final int wishlistPlaceCount;

    @Schema(description = "저장한 장소 목록")
    private final List<TripPlaceSelectionItemResponseDto> savedPlaces;

    @Schema(description = "위시리스트 장소 목록")
    private final List<TripPlaceSelectionItemResponseDto> wishlistPlaces;

    private TripPlaceSelectionResponseDto(
            Long tripId,
            String tripTitle,
            int savedPlaceCount,
            int wishlistPlaceCount,
            List<TripPlaceSelectionItemResponseDto> savedPlaces,
            List<TripPlaceSelectionItemResponseDto> wishlistPlaces
    ) {
        this.tripId = tripId;
        this.tripTitle = tripTitle;
        this.savedPlaceCount = savedPlaceCount;
        this.wishlistPlaceCount = wishlistPlaceCount;
        this.savedPlaces = savedPlaces;
        this.wishlistPlaces = wishlistPlaces;
    }

    public Long getTripId() {
        return tripId;
    }

    public String getTripTitle() {
        return tripTitle;
    }

    public int getSavedPlaceCount() {
        return savedPlaceCount;
    }

    public int getWishlistPlaceCount() {
        return wishlistPlaceCount;
    }

    public List<TripPlaceSelectionItemResponseDto> getSavedPlaces() {
        return savedPlaces;
    }

    public List<TripPlaceSelectionItemResponseDto> getWishlistPlaces() {
        return wishlistPlaces;
    }

    /**
     * 장소 선택 화면에 필요한 저장한 장소/위시리스트 목록 응답 DTO를 생성합니다.
     *
     * @param tripId 조회한 여행 PK
     * @param tripTitle 조회한 여행 제목
     * @param savedPlaces 저장한 장소 목록
     * @param wishlistPlaces 위시리스트 장소 목록
     * @return 저장한 장소/위시리스트 조회 응답 DTO
     */
    public static TripPlaceSelectionResponseDto of(
            Long tripId,
            String tripTitle,
            List<TripPlaceSelectionItemResponseDto> savedPlaces,
            List<TripPlaceSelectionItemResponseDto> wishlistPlaces
    ) {
        return new TripPlaceSelectionResponseDto(
                tripId,
                tripTitle,
                savedPlaces.size(),
                wishlistPlaces.size(),
                savedPlaces,
                wishlistPlaces
        );
    }
}
