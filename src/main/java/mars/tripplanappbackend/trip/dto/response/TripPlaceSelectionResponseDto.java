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

    @Schema(description = "저장한 장소 탭이 비어있는지 여부", example = "false")
    private final boolean savedPlaceEmpty;

    @Schema(description = "위시리스트 탭이 비어있는지 여부", example = "true")
    private final boolean wishlistPlaceEmpty;

    @Schema(description = "저장한 장소가 비었을 때 표시할 안내 문구", example = "저장한 장소가 없습니다.", nullable = true)
    private final String savedPlaceEmptyMessage;

    @Schema(description = "위시리스트가 비었을 때 표시할 안내 문구", example = "위시 리스트가 없습니다.", nullable = true)
    private final String wishlistPlaceEmptyMessage;

    @Schema(description = "저장한 장소 목록")
    private final List<TripPlaceSelectionItemResponseDto> savedPlaces;

    @Schema(description = "위시리스트 장소 목록")
    private final List<TripPlaceSelectionItemResponseDto> wishlistPlaces;

    private TripPlaceSelectionResponseDto(
            Long tripId,
            String tripTitle,
            int savedPlaceCount,
            int wishlistPlaceCount,
            boolean savedPlaceEmpty,
            boolean wishlistPlaceEmpty,
            String savedPlaceEmptyMessage,
            String wishlistPlaceEmptyMessage,
            List<TripPlaceSelectionItemResponseDto> savedPlaces,
            List<TripPlaceSelectionItemResponseDto> wishlistPlaces
    ) {
        this.tripId = tripId;
        this.tripTitle = tripTitle;
        this.savedPlaceCount = savedPlaceCount;
        this.wishlistPlaceCount = wishlistPlaceCount;
        this.savedPlaceEmpty = savedPlaceEmpty;
        this.wishlistPlaceEmpty = wishlistPlaceEmpty;
        this.savedPlaceEmptyMessage = savedPlaceEmptyMessage;
        this.wishlistPlaceEmptyMessage = wishlistPlaceEmptyMessage;
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

    public boolean isSavedPlaceEmpty() {
        return savedPlaceEmpty;
    }

    public boolean isWishlistPlaceEmpty() {
        return wishlistPlaceEmpty;
    }

    public String getSavedPlaceEmptyMessage() {
        return savedPlaceEmptyMessage;
    }

    public String getWishlistPlaceEmptyMessage() {
        return wishlistPlaceEmptyMessage;
    }

    public List<TripPlaceSelectionItemResponseDto> getSavedPlaces() {
        return savedPlaces;
    }

    public List<TripPlaceSelectionItemResponseDto> getWishlistPlaces() {
        return wishlistPlaces;
    }

    /**
     * 장소 선택 화면에 필요한 저장한 장소/위시리스트 목록 응답 DTO를 생성합니다.
     * 각 탭의 빈 상태 여부와 빈 상태 안내 문구까지 함께 구성해
     * 프론트엔드가 별도 조건문 없이 바텀시트 빈 화면을 렌더링할 수 있도록 합니다.
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
        boolean savedPlaceEmpty = savedPlaces.isEmpty();
        boolean wishlistPlaceEmpty = wishlistPlaces.isEmpty();

        return new TripPlaceSelectionResponseDto(
                tripId,
                tripTitle,
                savedPlaces.size(),
                wishlistPlaces.size(),
                savedPlaceEmpty,
                wishlistPlaceEmpty,
                savedPlaceEmpty ? "저장한 장소가 없습니다." : null,
                wishlistPlaceEmpty ? "위시 리스트가 없습니다." : null,
                savedPlaces,
                wishlistPlaces
        );
    }
}
